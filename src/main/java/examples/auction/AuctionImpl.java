package examples.auction;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import io.baratine.service.Asset;
import io.baratine.service.Id;
import io.baratine.service.IdAsset;
import io.baratine.service.Modify;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;
import io.baratine.timer.TimerService;

@Asset
public class AuctionImpl implements Auction
{
  private final static Logger log
    = Logger.getLogger(AuctionImpl.class.getName());

  @Id
  private IdAsset _id;

  private String _encodedId;

  private String _title;
  private int _startingBid;

  private ZonedDateTime _dateToClose;

  private String _ownerId;

  private ArrayList<Bid> _bids = new ArrayList<>();

  private BidImpl _lastBid;

  private State _state = State.INIT;

  private String _winnerId;
  private String _settlementId;

  private BoundState _boundState = BoundState.UNBOUND;

  @Inject
  private transient ServiceManager _manager;

  @Inject
  @Service("/settlement")
  private transient AuctionSettlementVault _settlementVault;

  @Inject
  @Service("/audit")
  private transient AuditService _audit;

  private transient AuctionEvents _events;

  public AuctionImpl()
  {
  }

  @Override
  @Modify
  public void create(AuctionDataInit initData,
                     Result<String> auctionId)
  {
    ZonedDateTime date = ZonedDateTime.now();
    date = date.plusSeconds(15);
    ZonedDateTime closingDate = date;

    log.log(Level.FINER, "XXX:0 " + _id + " / " + _id);

    _audit.auctionCreate(initData, Result.<Void>ignore());

    _ownerId = initData.getUserId();
    _title = initData.getTitle();
    _startingBid = initData.getStartingBid();
    _dateToClose = closingDate;

    _boundState = BoundState.BOUND;

    log.log(Level.FINER, "XXX:1 " + _id + " / " + _encodedId);

    _encodedId = _id.toString();

    log.log(Level.FINER, "XXX:2 " + _id + " / " + _encodedId);

    auctionId.ok(_encodedId);
  }

  private AuctionData getAuctionDataPublic()
  {
    return new AuctionData(getEncodedId(),
                           _title,
                           _startingBid,
                           _dateToClose,
                           _ownerId,
                           _bids,
                           _lastBid,
                           _state,
                           _winnerId,
                           _settlementId);
  }

  public String getWinner()
  {
    return _winnerId;
  }

  public void setWinner(String winner)
  {
    _winnerId = winner;
  }

  public void toOpen()
  {
    if (_state != State.INIT) {
      throw new IllegalStateException("Cannot open in " + _state);
    }

    _state = State.OPEN;
  }

  public void toClose()
  {
    if (_state != State.OPEN) {
      throw new IllegalStateException("Auction cannot be closed in " + _state);
    }

    _state = State.CLOSED;
  }

  @Modify
  public void open(Result<Boolean> result)
  {
    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException(String.format(
        "can't open auction %1$s in state %2$s",
        this,
        _boundState));

    if (_state == State.INIT) {
      _audit.auctionToOpen(getAuctionDataPublic(), Result.<Void>ignore());

      toOpen();

      startCloseTimer();

      result.ok(true);
    }
    else {
      throw new IllegalStateException(
        String.format("can't open auction %1s from state %2s",
                      getEncodedId(),
                      _state));
    }
  }

  public String getEncodedId()
  {
    return _encodedId;
  }

  private void startCloseTimer()
  {
    String url = "timer:///";

    ServiceManager manager = ServiceManager.current();
    TimerService timer = manager.service(url).as(TimerService.class);

    timer.runAt((x) -> closeOnTimer(Result.ignore()),
                getDateToClose().toInstant().toEpochMilli(),
                Result.ignore());

    log.finer("start timer for auction: " + getAuctionDataPublic());
  }

  void closeOnTimer(Result<Boolean> result)
  {
    if (_state == State.OPEN)
      close(result);
    else
      result.ok(true);
  }

  @Modify
  public void close(Result<Boolean> result)
  {
    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException();

    log.warning("close: " + this);

    if (_state == State.OPEN) {
      _audit.auctionToClose(getAuctionDataPublic(), Result.ignore());

      log.warning("close - 0: " + this);

      toClose();

      log.warning("close - 1: " + this);

      getEvents().onClose(getAuctionDataPublic());

      log.warning("close - 2: " + this);

      settle();

      log.warning("close - 3: " + this);

      result.ok(true);
    }
    else {
      throw new IllegalStateException(
        String.format("can't close auction %1s from state %2s",
                      getEncodedId(),
                      _state));

    }
  }

  @Override
  public void refund(Result<Boolean> result)
  {
    if (_state != State.SETTLED)
      throw new IllegalStateException();

    getAuctionSettlement(result.of((s, r) -> s.refund(r.of(t -> t
                                                                == AuctionSettlement.Status.ROLLED_BACK))));
  }

  private AuctionEvents getEvents()
  {
    if (_events == null) {
      String url = "event:///auction/" + getEncodedId();

      _events = _manager.service(url).as(AuctionEvents.class);
    }

    return _events;
  }

  private void getAuctionSettlement(Result<AuctionSettlement> result)
  {
    if (_settlementId == null) {
      _settlementVault.create(getAuctionDataPublic(),
                              result.of(s -> {
                                _settlementId = s;
                                return _manager.service("/settlement/" + s)
                                               .as(AuctionSettlement.class);
                              }));
    }
    else {
      result.ok(_manager.service("/settlement/" + getSettlementId())
                        .as(AuctionSettlement.class));
    }
  }

  private void settle()
  {
    Bid bid = getLastBid();

    if (bid == null)
      return;

    getAuctionSettlement((s, e) -> s.settle(bid, Result.ignore()));
  }

  public Auction.Bid getLastBid()
  {
    return _lastBid;
  }

  public String getLastBidder()
  {
    Auction.Bid lastBid = getLastBid();

    if (lastBid != null) {
      return lastBid.getUserId();
    }
    else {
      return null;
    }
  }

  @Modify
  public void bid(AuctionBid bid, Result<Boolean> result)
    throws IllegalStateException
  {
    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException();

    _audit.auctionBid(getAuctionDataPublic(), bid, Result.<Void>ignore());

    boolean isAccepted = bid(bid.getUser(), bid.getBid());

    if (isAccepted) {
      _audit.auctionBidAccept(bid, Result.ignore());

      getEvents().onBid(getAuctionDataPublic());

      result.ok(true);
    }
    else {
      _audit.auctionBidReject(bid, Result.ignore());

      result.ok(false);
    }
  }

  public boolean bid(String bidderId, int bid)
    throws IllegalStateException
  {
    if (_state != State.OPEN) {
      throw new IllegalStateException("auction cannot be bid in " + _state);
    }

    Auction.Bid last = getLastBid();

    if (last == null || bid > last.getBid()) {
      BidImpl nextBid = new BidImpl(bidderId, _encodedId, bid);

      _bids.add(nextBid);

      _lastBid = nextBid;

      return true;
    }
    else {
      return false;
    }
  }

  @Override
  @Modify
  public void setAuctionWinner(String user, Result<Boolean> result)
  {
    if (_boundState != BoundState.BOUND)
      throw new IllegalStateException();

    setWinner(user);

    //TODO:
    getEvents().onSettled(getAuctionDataPublic());

    result.ok(true);
  }

  @Override
  @Modify
  public void clearAuctionWinner(String user, Result<Boolean> result)
  {
    setWinner(null);

    result.ok(true);
  }

  @Override
  @Modify
  public void setSettled(Result<Boolean> result)
  {
    toSettled();

    result.ok(true);

    getEvents().onSettled(getAuctionDataPublic());
  }

  @Override
  @Modify
  public void setRolledBack(Result<Boolean> result)
  {
    toRolledBack();

    getEvents().onRolledBack(getAuctionDataPublic());
  }

  public void get(Result<AuctionData> result)
  {
    if (log.isLoggable(Level.FINER))
      log.finer(String.format("@%1$d get %2$s %3$s",
                              System.identityHashCode(this),
                              getEncodedId(),
                              getAuctionDataPublic()));

    result.ok(getAuctionDataPublic());
  }

  @Override
  public void getSettlementId(Result<String> result)
  {
    result.ok(getSettlementId());
  }

  public void toSettled()
  {
    if (_state != State.CLOSED)
      throw new IllegalStateException();

    _state = State.SETTLED;
  }

  public String getSettlementId()
  {
    return _settlementId;
  }

  public void toRolledBack()
  {
    _state = State.ROLLED_BACK;
  }

  public ZonedDateTime getDateToClose()
  {
    return _dateToClose;
  }

  public static class BidImpl implements Auction.Bid, Comparable<Auction.Bid>,
    Serializable
  {
    private String _auctionId;
    private String _userId;
    private int _bid;

    public BidImpl()
    {

    }

    BidImpl(String userId, String auctionId, int bid)
    {
      _userId = userId;
      _auctionId = auctionId;
      _bid = bid;
    }

    @Override
    public String getAuctionId()
    {
      return _auctionId;
    }

    @Override
    public int getBid()
    {
      return _bid;
    }

    @Override
    public String getUserId()
    {
      return _userId;
    }

    @Override
    public int compareTo(Auction.Bid o)
    {
      return _bid - o.getBid();
    }

    @Override
    public String toString()
    {
      return getClass().getSimpleName()
             + "@" + System.identityHashCode(this) + "["
             + _userId + "," + _bid + "]";
    }
  }

  @Override
  public String toString()
  {
    return AuctionImpl.class.getSimpleName()
           + "["
           + getEncodedId()
           + ", "
           + _boundState
           + ", "
           + getAuctionDataPublic()
           + "]";
  }

  enum BoundState
  {
    UNBOUND,
    BOUND
  }
}

