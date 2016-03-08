package examples.auction;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import examples.auction.AuctionSettlement.Status;
import io.baratine.service.Data;
import io.baratine.service.Id;
import io.baratine.service.Ids;
import io.baratine.service.Modify;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;
import io.baratine.service.ServiceRef;
import io.baratine.timer.TimerService;

@Data
public class AuctionImpl implements Auction
{
  private final static Logger log
    = Logger.getLogger(AuctionImpl.class.getName());

  @Id
  private long _id;

  private String _encodedId;

  private AuctionDataPublic _auctionData;

  private State _state = State.UNBOUND;

  @Inject
  private transient ServiceManager _manager;

  @Inject
  @Service("/audit")
  private transient AuditService _audit;

  private transient AuctionEvents _events;

  private transient ServiceRef _settlement;

  public AuctionImpl()
  {
  }

  @Modify
  public void create(AuctionDataInit initData,
                     Result<Long> result)
  {
/*
    if (_state != State.UNBOUND)
      throw new IllegalStateException();
*/

    ZonedDateTime date = ZonedDateTime.now();
    date = date.plusSeconds(15);
    ZonedDateTime closingDate = date;

    _audit.auctionCreate(initData, Result.<Void>ignore());

    AuctionDataPublic auctionData
      = new AuctionDataPublic(getEncodedId(), initData, closingDate);

    _auctionData = auctionData;

    _state = State.BOUND;

    result.ok(_id);
  }

  public String getEncodedId()
  {
    if (_encodedId == null)
      _encodedId = Ids.encode(_id);

    return _encodedId;
  }

  @Modify
  public void open(Result<Boolean> result)
  {
    if (_state == State.UNBOUND)
      throw new IllegalStateException(String.format(
        "can't open auction %1$s in state %2$s",
        this,
        _state));

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
    TimerService timer = manager.service(url).as(TimerService.class);

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

      if (_settlement != null)
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

      _events = _manager.service(url).as(AuctionEvents.class);
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
    if (log.isLoggable(Level.FINER))
      log.finer(String.format("@%1$d get %2$s %3$s",
                              System.identityHashCode(this),
                              getEncodedId(),
                              _auctionData));

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
    return AuctionImpl.class.getSimpleName()
           + "["
           + getEncodedId()
           + ", "
           + _state
           + ", "
           + _auctionData
           + "]";
  }

  enum State
  {
    UNBOUND,
    BOUND
  }
}

