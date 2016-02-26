package examples.auction;

import java.io.Serializable;

public class Bid implements Serializable
{
  private long _user;
  private int _bid;

  public Bid()
  {
  }

  public Bid(long user, int bid)
  {
    _user = user;
    _bid = bid;
  }

  public long getUser()
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
