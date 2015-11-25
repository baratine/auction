package examples.auction;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 *
 */
public class AuctionDataPublic implements Serializable
{
  private String _id;
  private String _title;
  private int _startingBid;

  private ZonedDateTime _dateToClose;

  private String _ownerId;

  private ArrayList<Bid> _bids = new ArrayList<>();

  private BidImpl _lastBid;

  private State _state = State.INIT;

  //user id
  private String _winner;

  public AuctionDataPublic()
  {
  }

  public AuctionDataPublic(String id,
                           AuctionDataInit initData,
                           ZonedDateTime closingDate)
  {
    _id = id;
    _ownerId = initData.getUserId();
    _title = initData.getTitle();
    _startingBid = initData.getStartingBid();
    _dateToClose = closingDate;
  }

  public String getId()
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

  public String getOwnerId()
  {
    return _ownerId;
  }

  public boolean bid(String bidderId, int bid)
    throws IllegalStateException
  {
    if (_state != State.OPEN) {
      throw new IllegalStateException("auction cannot be bid in " + _state);
    }

    Bid last = getLastBid();

    if (last == null || bid > last.getBid()) {
      BidImpl nextBid = new BidImpl(bidderId, bid);

      _bids.add(nextBid);

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

  public String getWinner()
  {
    if (_winner != null)
      return _winner;

    Bid lastBid = getLastBid();

    if (lastBid != null) {
      return lastBid.getUserId();
    }
    else {
      return null;
    }
  }

  public void setWinner(String winner)
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
    StringBuilder sb = new StringBuilder();

    sb.append(getClass().getSimpleName())
      .append("@")
      .append(System.identityHashCode(this))
      .append(
        "[");
    sb.append(_id);
    sb.append(", ").append(_title);
    sb.append(", bid=").append(_lastBid);
    sb.append("]");

    return sb.toString();
  }

  public void toSettled()
  {
    if (_state != State.CLOSED)
      throw new IllegalStateException();

    _state = State.SETTLED;
  }

  public void toRolledBack()
  {
    _state = State.ROLLED_BACK;
  }

  static enum State
  {
    INIT,
    OPEN,
    CLOSED,
    SETTLED,
    ROLLED_BACK
  }

  public interface Bid
  {
    String getUserId();

    int getBid();
  }

  static class BidImpl implements Bid, Comparable<Bid>, Serializable
  {
    private String _userId;
    private int _bid;

    public BidImpl()
    {

    }

    BidImpl(String userId, int bid)
    {
      _userId = userId;
      _bid = bid;
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
    public int compareTo(Bid o)
    {
      return _bid - o.getBid();
    }

    @Override
    public String toString()
    {
      return getClass().getSimpleName()
             + "@"
             + System.identityHashCode(this)
             + "["
             + _userId
             + ","
             + _bid
             + "]";
    }
  }
}
