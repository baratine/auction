package examples.auction;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 *
 */
public class AuctionDataPublic implements Serializable
{
  private long _id;
  private String _title;
  private int _startingBid;

  private ZonedDateTime _dateToClose;

  private long _ownerId;

  private ArrayList<Bid> _bids = new ArrayList<>();

  private BidImpl _lastBid;

  private State _state = State.INIT;

  //user id
  private long _winner = -1;
  private String _settlementId;

  public AuctionDataPublic()
  {
  }

  public AuctionDataPublic(long id,
                           AuctionDataInit initData,
                           ZonedDateTime closingDate)
  {
    _id = id;
    _ownerId = initData.getUserId();
    _title = initData.getTitle();
    _startingBid = initData.getStartingBid();
    _dateToClose = closingDate;
    _settlementId = UUID.randomUUID().toString();
  }

  public long getId()
  {
    return _id;
  }

  public String getTitle()
  {
    return _title;
  }

  public void setTitle(String title)
  {
    _title = title;
  }

  public int getStartingBid()
  {
    return _startingBid;
  }

  public void setStartingBid(int startingBid)
  {
    _startingBid = startingBid;
  }

  public ZonedDateTime getDateToClose()
  {
    return _dateToClose;
  }

  public long getOwnerId()
  {
    return _ownerId;
  }

  public boolean bid(long bidderId, int bid)
    throws IllegalStateException
  {
    if (_state != State.OPEN) {
      throw new IllegalStateException("auction cannot be bid in " + _state);
    }

    Bid last = getLastBid();

    if (last == null || bid > last.getBid()) {
      BidImpl nextBid = new BidImpl(bidderId, _id, bid);

      //_bids.add(nextBid);

      _lastBid = nextBid;

      return true;
    }
    else {
      return false;
    }
  }

  public Bid getLastBid()
  {
    return _lastBid;
  }

  public long getLastBidder()
  {
    Bid lastBid = getLastBid();

    if (lastBid != null) {
      return lastBid.getUserId();
    }
    else {
      return -1;
    }
  }

  public long getWinner()
  {
    return _winner;
  }

  public void setWinner(long winner)
  {
    _winner = winner;
  }

  public State getState()
  {
    return _state;
  }

  public void toOpen()
  {
    if (_state != State.INIT) {
      throw new IllegalStateException("Cannot open in " + _state);
    }

    _title = _title + "open";

    _state = State.OPEN;
  }

  public void toClose()
  {
    if (_state != State.OPEN) {
      throw new IllegalStateException("Auction cannot be closed in " + _state);
    }

    _state = State.CLOSED;
  }

  @Override
  public String toString()
  {
    String toString
      = String.format("%1$s@%2$d[%3$s, %4$s, %5$s, %6$s, %7$s]",
                      getClass().getSimpleName(),
                      System.identityHashCode(this),
                      _id,
                      _title,
                      _lastBid,
                      _winner,
                      _state);
    return toString;
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

  enum State
  {
    INIT,
    OPEN,
    CLOSED,
    SETTLED,
    ROLLED_BACK
  }

  public interface Bid
  {
    long getAuctionId();

    long getUserId();

    int getBid();
  }

  static class BidImpl implements Bid, Comparable<Bid>, Serializable
  {
    private long _auctionId;
    private long _userId;
    private int _bid;

    public BidImpl()
    {

    }

    BidImpl(long userId, long auctionId, int bid)
    {
      _userId = userId;
      _auctionId = auctionId;
      _bid = bid;
    }

    @Override
    public long getAuctionId()
    {
      return _auctionId;
    }

    @Override
    public int getBid()
    {
      return _bid;
    }

    @Override
    public long getUserId()
    {
      return _userId;
    }

    @Override
    public int compareTo(Bid o)
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
