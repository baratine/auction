package examples.auction;

import java.util.HashSet;
import java.util.Set;

public class UserDataPublic
{
  private long _id;
  private String _name;

  private Set<Long> _wonAuctions = new HashSet<>();

  public UserDataPublic()
  {
  }

  public UserDataPublic(UserData userData)
  {
    _id = userData.getId();
    _name = userData.getName();
    _wonAuctions = new HashSet<>(userData.getWonAuctions());
  }

  public long getId()
  {
    return _id;
  }

  public String getName()
  {
    return _name;
  }

  public Set<Long> getWonAuctions()
  {
    return _wonAuctions;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _id + "," + _name + "]";
  }
}
