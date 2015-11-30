package examples.auction;

public class UserDataPublic
{
  private String _id;
  private String _name;
  private String _password;
  private boolean _isAdmin;

  public UserDataPublic()
  {
  }

  public UserDataPublic(String id,
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

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _id + "," + _name + "]";
  }
}
