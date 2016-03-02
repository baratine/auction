package examples.auction;

import java.io.Serializable;

public class Bid implements Serializable
{
  private String _user;
  private int _bid;

  public Bid()
  {
  }

  public Bid(String user, int bid)
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
