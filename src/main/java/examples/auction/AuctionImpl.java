package examples.auction;

import examples.auction.AuctionSettlement.Status;
import io.baratine.service.Modify;
import io.baratine.service.OnLoad;
import io.baratine.service.OnSave;
import io.baratine.service.Result;
import io.baratine.service.ServiceManager;
import io.baratine.service.ServiceRef;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.timer.TimerService;

import java.time.ZonedDateTime;
import java.util.logging.Logger;

public class AuctionImpl implements Auction
{
  private final static Logger log
    = Logger.getLogger(AuctionImpl.class.getName());

  private ServiceManager _manager;
  private DatabaseService _db;

  private String _id;
  private AuctionDataPublic _auctionData;

  private AuctionEvents _events;

  private ServiceRef _auctionManager;

  private AuditService _audit;

  private State _state;

  private ServiceRef _settlement;

  public AuctionImpl()
  {
  }

  public AuctionImpl(ServiceManager manager,
                     ServiceRef auctionManager,
                     DatabaseService db,
                     String id)
  {
    _manager = manager;
    _auctionManager = auctionManager;
    _db = db;
    _id = id;

    ServiceRef auditRef = manager.lookup("public:///audit");

    _audit = auditRef.as(AuditService.class);

    _settlement = manager.lookup("public:///settlement");

    _state = State.UNBOUND;
  }

  @Modify
  public void create(AuctionDataInit initData,
                     Result<String> result)
  {
    if (_state != State.UNBOUND)
      throw new IllegalStateException();

    ZonedDateTime date = ZonedDateTime.now();
    date = date.plusSeconds(15);
    ZonedDateTime closingDate = date;

    _audit.auctionCreate(initData, Result.<Void>ignore());

    AuctionDataPublic auctionData
      = new AuctionDataPublic(_id, initData, closingDate);

    _auctionData = auctionData;

    _state = State.BOUND;

    result.ok(_id);
  }

  @OnSave
  public void save(Result<Boolean> result)
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    _audit.auctionSave(_auctionData, Result.<Void>ignore());

    _db.exec("insert into auction (id, title, value) values (?,?,?)",
             result.of(o -> resultFromSave(o)),
             _id,
             _auctionData.getTitle(),
             _auctionData);

    log.finer(String.format("async-saved auction %1$s to db", _auctionData));
  }

  private boolean resultFromSave(Object obj)
  {
    log.finer(String.format("executed save auction to db with result %1$s",
                            obj));

    return obj != null;
  }

  @OnLoad
  public void load(Result<Boolean> result)
  {
    if (_state != State.UNBOUND)
      throw new IllegalStateException();

    _db.findOne("select value from auction where id=?",
                result.of(c -> loadComplete(c)),
                _id);
  }

  boolean loadComplete(Cursor c)
  {
    if (c != null) {
      _auctionData = (AuctionDataPublic) c.getObject(1);

      _state = State.BOUND;
    }

    _audit.auctionLoad(_auctionData, Result.<Void>ignore());

    return _auctionData != null;
  }

  @Modify
  public void open(Result<Boolean> result)
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException("can't open auction %1$s in state ");

    if (_auctionData.getState() == AuctionDataPublic.State.INIT) {
      _audit.auctionToOpen(_auctionData, Result.<Void>ignore());

      _auctionData.toOpen();

      startCloseTimer();

      result.ok(true);
    }
    else {
      throw new IllegalStateException(
        String.format("can't open auction %1s from state %2s",
                      _auctionData.getId(),
                      _auctionData.getState()));
    }
  }

  private void startCloseTimer()
  {
    String url = "timer:///";

    ServiceManager manager = ServiceManager.current();
    TimerService timer = manager.lookup(url).as(TimerService.class);

    timer.runAt((x) -> closeOnTimer(Result.ignore()),
                _auctionData.getDateToClose().toInstant().toEpochMilli(),
                Result.ignore());

    log.finer("start timer for auction: " + _auctionData);
  }

  void closeOnTimer(Result<Boolean> result)
  {
    if (_auctionData.getState() == AuctionDataPublic.State.OPEN)
      close(result);
    else
      result.ok(true);
  }

  @Modify
  public void close(Result<Boolean> result)
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    if (_auctionData.getState() == AuctionDataPublic.State.OPEN) {
      _audit.auctionToClose(_auctionData, Result.ignore());

      _auctionData.toClose();

      getEvents().onClose(_auctionData);

      settle();

      result.ok(true);
    }
    else {
      throw new IllegalStateException(
        String.format("can't close auction %1s from state %2s",
                      _auctionData.getId(),
                      _auctionData.getState()));

    }
  }

  @Override
  public void refund(Result<Boolean> result)
  {
    if (_auctionData.getState() != AuctionDataPublic.State.SETTLED)
      throw new IllegalStateException();

    getAuctionSettlement()
      .refund(result.of(s -> s.equals(Status.ROLLED_BACK)));
  }

  private AuctionEvents getEvents()
  {
    if (_events == null) {
      String url = "event:///auction/" + _auctionData.getId();

      _events = _manager.lookup(url).as(AuctionEvents.class);
    }

    return _events;
  }

  private AuctionSettlement getAuctionSettlement()
  {
    String settlementUri = "/" + _auctionData.getSettlementId();

    AuctionSettlement settlement
      = _settlement.lookup(settlementUri).as(AuctionSettlement.class);

    return settlement;
  }

  private void settle()
  {
    AuctionDataPublic.Bid bid = _auctionData.getLastBid();

    if (bid == null)
      return;

    AuctionSettlement settlement = getAuctionSettlement();

    settlement.settle(bid, Result.ignore());
  }

  @Modify
  public void bid(Bid bid, Result<Boolean> result)
    throws IllegalStateException
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    _audit.auctionBid(_auctionData, bid, Result.<Void>ignore());

    boolean isAccepted = _auctionData.bid(bid.getUser(), bid.getBid());

    if (isAccepted) {
      _audit.auctionBidAccept(bid, Result.ignore());

      getEvents().onBid(_auctionData);

      result.ok(true);
    }
    else {
      _audit.auctionBidReject(bid, Result.ignore());

      result.ok(false);
    }
  }

  @Override
  @Modify
  public void setAuctionWinner(String user, Result<Boolean> result)
  {
    if (_state != State.BOUND)
      throw new IllegalStateException();

    _auctionData.setWinner(user);

    //TODO:
    getEvents().onSettled(_auctionData);

    result.ok(true);
  }

  @Override
  @Modify
  public void clearAuctionWinner(String user, Result<Boolean> result)
  {
    System.out.println("AuctionImpl.clearAuctionWinner");

    _auctionData.setWinner(null);

    result.ok(true);
  }

  @Override
  @Modify
  public void setSettled(Result<Boolean> result)
  {
    _auctionData.toSettled();

    result.ok(true);

    getEvents().onSettled(_auctionData);
  }

  @Override
  @Modify
  public void setRolledBack(Result<Boolean> result)
  {
    _auctionData.toRolledBack();

    getEvents().onRolledBack(_auctionData);
  }

  public void get(Result<AuctionDataPublic> result)
  {
    log.finer("get auction data public @"
              + System.identityHashCode(this)
              + " : "
              + _auctionData);

    result.ok(_auctionData);
  }

  @Override
  public void getSettlementId(Result<String> result)
  {
    result.ok(_auctionData.getSettlementId());
  }

  @Override
  public String toString()
  {
    return AuctionImpl.class.getSimpleName() + "[" + _id + ", " + _state + "]";
  }

  enum State
  {
    UNBOUND,
    BOUND
  }
}

