package examples.auction;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import io.baratine.event.EventsSync;
import io.baratine.service.Modify;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.timer.Timers;
import io.baratine.vault.Asset;
import io.baratine.vault.Id;
import io.baratine.vault.IdAsset;

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
  private transient Services _manager;

  @Inject
  @Service("/AuctionSettlement")
  private transient AuctionSettlementVault _settlementVault;

  @Inject
  @Service("/audit")
  private transient AuditService _audit;

  @Inject
  private transient EventsSync _events;

  private transient AuctionEvents _auctionEvents;

  public AuctionImpl()
  {
  }

  @Override
  @Modify
  public void create(AuctionDataInit initData,
                     Result<IdAsset> auctionId)
  {
    ZonedDateTime date = ZonedDateTime.now();
    date = date.plusSeconds(15);
    ZonedDateTime closingDate = date;

    _audit.auctionCreate(initData, Result.<Void>ignore());

    _ownerId = initData.getUserId();
    _title = initData.getTitle();
    _startingBid = initData.getStartingBid();
    _dateToClose = closingDate;

    _boundState = BoundState.BOUND;

    _encodedId = _id.toString();

    auctionId.ok(_id);
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

  private void startCloseTimer()
  {
    String url = "timer:///";

    Services manager = Services.current();
    Timers timer = manager.service(url).as(Timers.class);

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

      toClose();

      getAuctionEvents().onClose(getAuctionDataPublic());

      settle();

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

  private void getAuctionSettlement(Result<AuctionSettlement> result)
  {
    log.finer("getAuctionSettlement: 0 " + _settlementId);
    if (_settlementId == null) {
      _settlementVault.create(getAuctionDataPublic(),
                              result.of(s -> {
                                _settlementId = s.toString();
                                log.finer("getAuctionSettlement: 1 "
                                          + _settlementId + ", " + _manager);
                                try {
                                  AuctionSettlement settlement
                                    = _manager.service(AuctionSettlement.class,
                                                       _settlementId);

                                  log.finer("getAuctionSettlement: 2 "
                                            + settlement);

                                  return settlement;
                                } catch (Exception e) {
                                  log.log(Level.SEVERE, e.getMessage(), e);

                                  throw new RuntimeException(e);
                                }

                              }));
    }
    else {
      log.finer("getAuctionSettlement: 2 " + _settlementId);

      result.ok(_manager.service(AuctionSettlement.class, _settlementId));
    }
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

  public String getEncodedId()
  {
    return _encodedId;
  }

  private void settle()
  {
    Bid bid = getLastBid();

    if (bid == null)
      return;

    getAuctionSettlement((s, e) -> {
      s.settle(bid, Result.ignore());
    });
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

  public Auction.Bid getLastBid()
  {
    return _lastBid;
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

      getAuctionEvents().onBid(getAuctionDataPublic());

      result.ok(true);
    }
    else {
      _audit.auctionBidReject(bid, Result.ignore());

      result.ok(false);
    }
  }

  private AuctionEvents getAuctionEvents()
  {
    if (_auctionEvents == null)
      _auctionEvents = _events.publisherPath(_encodedId, AuctionEvents.class);

    return _auctionEvents;
  }

  private boolean bid(String bidderId, int bid)
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
    getAuctionEvents().onSettled(getAuctionDataPublic());

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

    getAuctionEvents().onSettled(getAuctionDataPublic());
  }

  public void toSettled()
  {
    if (_state != State.CLOSED)
      throw new IllegalStateException();

    _state = State.SETTLED;
  }

  @Override
  @Modify
  public void setRolledBack(Result<Boolean> result)
  {
    toRolledBack();

    getAuctionEvents().onRolledBack(getAuctionDataPublic());
  }

  public void toRolledBack()
  {
    _state = State.ROLLED_BACK;
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

  public String getSettlementId()
  {
    return _settlementId;
  }

  public ZonedDateTime getDateToClose()
  {
    return _dateToClose;
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
           + "]@" + System.identityHashCode(this);
  }

  enum BoundState
  {
    UNBOUND,
    BOUND
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
}

