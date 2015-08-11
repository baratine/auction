package examples.auction;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.core.ServiceManager;
import io.baratine.core.ServiceRef;
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

    _audit = manager.lookup("pod://audit/audit").as(AuditService.class);

    _state = State.UNBOUND;
  }

  @Modify
  public void create(AuctionDataInit initData,
                     Result<String> result)
  {
    if (_state != State.UNBOUND)
      throw new IllegalStateException();

    ZonedDateTime date = ZonedDateTime.now();
    date = date.plusSeconds(30);
    ZonedDateTime closingDate = date;

    AuctionDataPublic auctionData
      = new AuctionDataPublic(_id, initData, closingDate);

    log.finer("create auction: " + auctionData);

    _auctionData = auctionData;

/*
    _db.exec("insert into auction (id, title, value) values (?,?,?)",
             result.from(o -> _id), _id, initData.getTitle(), auctionData);
*/

    _state = State.BOUND;

    result.complete(_id);
  }

  @OnSave
  public void save(Result<Boolean> result)
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    log.finer("save auction @"
              + System.identityHashCode(this)
              + ": "
              + _auctionData);

    _db.exec("insert into auction (id, title, value) values (?,?,?)",
             result.from(o -> o != null),
             _id,
             _auctionData.getTitle(),
             _auctionData);

/*
    _db.exec("update auction set title=?, value=? where id=?",
             result.from(o -> o != null),
             _auctionData.getTitle(),
             _auctionData,
             _auctionData.getId());
*/
  }

  @OnLoad
  public void load(Result<Boolean> result)
  {
    if (_state != State.UNBOUND)
      throw new IllegalStateException();

    _db.findOne("select value from auction where id=?",
                result.from(c -> loadComplete(c)),
                _id);
  }

  boolean loadComplete(Cursor c)
  {
    if (c != null) {
      _auctionData = (AuctionDataPublic) c.getObject(1);
    }

    log.finer("load auction [" + _id + "]: " + _auctionData);

    return _auctionData != null;
  }

  @Modify
  public void open(Result<Boolean> result)
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    if (_auctionData.getState() == AuctionDataPublic.State.INIT) {
      _auctionData.toOpen();

      log.finer("open auction: " + _auctionData);

      _audit.audit(AuditService.AuditEvent.OPEN,
                   _auctionData,
                   Result.<Void>ignore());

      startTimer();

      result.complete(true);
    }
    else {
      throw new IllegalStateException(
        String.format("can't open auction %1s from state %2s",
                      _auctionData.getId(),
                      _auctionData.getState()));
    }
  }

  private void startTimer()
  {
    String url = "timer:///";

    ServiceManager manager = ServiceManager.getCurrent();
    TimerService timer = manager.lookup(url).as(TimerService.class);

    Auction service = _auctionManager.lookup("/" + _id).as(Auction.class);

    timer.runAt((x) -> service.close(Result.ignore()),
                _auctionData.getDateToClose().toInstant().toEpochMilli(),
                Result.ignore());

    log.finer("start timer for auction: " + _auctionData);
  }

  @Modify
  public void bid(String userId,
                  int bid,
                  Result<Boolean> result)
    throws IllegalStateException
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    log.finer("bid auction: " + _auctionData);

    boolean isSuccess = _auctionData.bid(userId, bid);

    if (isSuccess) {
      getEvents().onBid(_auctionData);

      result.complete(true);

      _audit.audit(AuditService.AuditEvent.BID,
                   _auctionData,
                   Result.<Void>ignore());
    }
    else {
      result.complete(false);
    }
  }

  private AuctionEvents getEvents()
  {
    if (_events == null) {
      String url = "event://auction/auction/" + _auctionData.getId();

      _events = _manager.lookup(url).as(AuctionEvents.class);
    }

    return _events;
  }

  public void get(Result<AuctionDataPublic> result)
  {
    log.finer("get auction data public @"
              + System.identityHashCode(this)
              + " : "
              + _auctionData);

    result.complete(_auctionData);
  }

  @Modify
  public void close(Result<Boolean> result)
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    if (_auctionData.getState() == AuctionDataPublic.State.OPEN) {
      _auctionData.toClose();

      log.finer("close auction: " + _auctionData);

      getEvents().onClose(_auctionData);

      result.complete(true);

      _audit.audit(AuditService.AuditEvent.CLOSE,
                   _auctionData,
                   Result.<Void>ignore());
    }
    else {
      throw new IllegalStateException(
        String.format("can't close auction %1s from state %2s",
                      _auctionData.getId(),
                      _auctionData.getState()));

    }
  }

  enum State
  {
    UNBOUND,
    BOUND
  }
}

