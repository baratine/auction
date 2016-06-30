package examples.auction;

import java.util.Set;

public class UserData
{
  private String _encodedId;

  private String _name;

  private Set<String> _wonAuctions;

  public UserData()
  {
  }

  public UserData(String userId,
                  String name,
                  Set<String> wonAuctions)
  {
    _encodedId = userId;
    _name = name;
    _wonAuctions = wonAuctions;
  }

  public String getEncodedId()
  {
    return _encodedId;
  }

  public String getName()
  {
    return _name;
  }

  public Set<String> getWonAuctions()
  {
    return _wonAuctions;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _encodedId + "," + _name + "]";
  }
}
