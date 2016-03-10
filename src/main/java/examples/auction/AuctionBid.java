package examples.auction;

import java.io.Serializable;

public class AuctionBid implements Serializable
{
  private String _user;
  private int _bid;

  public AuctionBid()
  {
  }

  public AuctionBid(String user, int bid)
  {
    _user = user;
    _bid = bid;
  }

  public String getUser()
  {
    return _user;
  }

  public int getBid()
  {
    return _bid;
  }

  public String toString()
  {
    return String.format("Bid[%1$s, %2$s]", _user, _bid);
  }
}
