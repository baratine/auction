package examples.auction;

import java.io.Serializable;

public class AuctionDataInit implements Serializable
{
  private long _userId;
  private String _title;
  private int _startingBid;

  public AuctionDataInit()
  {
  }

  public AuctionDataInit(long userId, String title, int startingBid)
  {
    _userId = userId;
    _title = title;
    _startingBid = startingBid;
  }

  public long getUserId()
  {
    return _userId;
  }

  public String getTitle()
  {
    return _title;
  }

  public int getStartingBid()
  {
    return _startingBid;
  }

  @Override
  public String toString()
  {
    return "AuctionDataInit[" + _title + ", " + _startingBid + ']';
  }
}
