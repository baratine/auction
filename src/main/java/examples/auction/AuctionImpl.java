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

    ServiceRef auditRef = manager.lookup("pod://audit/audit");

    try {
      _audit = auditRef.as(AuditService.class);
    } catch (Throwable t) {
      t.printStackTrace();
      log.finer(String.format("this: %1$s", this));
      log.finer(String.format("this.getClass().getClassLoader(): %1$s",
                              this.getClass().getClassLoader()));
      log.finer(String.format("this._manager: %1$s", this._manager));
      log.finer(String.format("Thread.contextClassLoader(): %1$s",
                              Thread.currentThread().getContextClassLoader()));
      log.finer(String.format("AuditService.class.getClassLoader(): %1$s",
                              AuditService.class.getClassLoader()));

      log.finer(String.format("auditRef.getManager(): %1$s",
                              auditRef.getManager()));

      log.finer(String.format("auditRef: %1$s", auditRef));

      log.finer(String.format("auditRef.getClass().getClassLoader(): %1$s",
                              auditRef.getClass().getClassLoader()));

      for (Class c : auditRef.getClass().getInterfaces()) {
        log.finer(String.format("auditRef.interface : %1$s : %2$s", c,
                                getClass().getClassLoader()));

      }
    }

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

    _audit.auctionCreate(initData, Result.<Void>ignore());

    AuctionDataPublic auctionData
      = new AuctionDataPublic(_id, initData, closingDate);

    _auctionData = auctionData;

    _state = State.BOUND;

    result.complete(_id);
  }

  @OnSave
  public void save(Result<Boolean> result)
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    _audit.auctionSave(_auctionData, Result.<Void>ignore());

    _db.exec("insert into auction (id, title, value) values (?,?,?)",
             result.from(o -> resultFromSave(o)),
             _id,
             _auctionData.getTitle(),
             _auctionData);

    log.finer(String.format("async-saved auction %1$s to db", _auctionData));

/*
    _db.exec("update auction set title=?, value=? where id=?",
             result.from(o -> o != null),
             _auctionData.getTitle(),
             _auctionData,
             _auctionData.getId());
*/
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
                result.from(c -> loadComplete(c)),
                _id);
  }

  boolean loadComplete(Cursor c)
  {
    if (c != null) {
      _auctionData = (AuctionDataPublic) c.getObject(1);
    }

    _audit.auctionLoad(_auctionData, Result.<Void>ignore());

    return _auctionData != null;
  }

  @Modify
  public void open(Result<Boolean> result)
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    if (_auctionData.getState() == AuctionDataPublic.State.INIT) {
      _audit.auctionToOpen(_auctionData, Result.<Void>ignore());

      _auctionData.toOpen();

      startCloseTimer();

      result.complete(true);
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

    Auction service = _auctionManager.lookup("/" + _id).as(Auction.class);

    timer.runAt((x) -> service.close(Result.ignore()),
                _auctionData.getDateToClose().toInstant().toEpochMilli(),
                Result.ignore());

    log.finer("start timer for auction: " + _auctionData);
  }

  @Modify
  public void bid(Bid bid, Result<Boolean> result)
    throws IllegalStateException
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException();

    _audit.auctionBid(_auctionData, bid, Result.<Void>ignore());

    boolean isSuccess = _auctionData.bid(bid.getUser(), bid.getBid());

    if (isSuccess) {
      _audit.auctionBidAccept(bid, Result.<Void>ignore());

      getEvents().onBid(_auctionData);

      result.complete(true);
    }
    else {
      _audit.auctionBidReject(bid, Result.<Void>ignore());

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
      _audit.auctionToClose(_auctionData, Result.<Void>ignore());

      _auctionData.toClose();

      getEvents().onClose(_auctionData);

      result.complete(true);
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

