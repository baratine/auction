package examples.auction;

import core.db.Column;
import core.db.Table;

import java.util.HashSet;
import java.util.Set;

@Table(name = "user")
@Column(name = "value")
public class UserData
{
  @Column(name = "id", pk = true)
  private String _id;
  @Column(name = "name", pk = false)
  private String _name;
  private String _password;
  private boolean _isAdmin;

  private Set<String> _wonAuctions = new HashSet<>();

  public UserData()
  {
  }

  public UserData(String id,
                  String name,
                  String password,
                  boolean isAdmin)
  {
    _id = id;
    _name = name;
    _password = password;
    _isAdmin = isAdmin;
  }

  public String getId()
  {
    return _id;
  }

  public String getName()
  {
    return _name;
  }

  public String getDigest()
  {
    return _password;
  }

  public boolean isAdmin()
  {
    return _isAdmin;
  }

  public Set<String> getWonAuctions()
  {
    return _wonAuctions;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _id + "," + _name + "]";
  }

  public void addWonAuction(String auctionId)
  {
    _wonAuctions.add(auctionId);
  }

  public void removeWonAuction(String auctionId)
  {
    _wonAuctions.remove(auctionId);
  }
}
