package examples.auction;

public class UserDataPublic
{
  private String _id;
  private String _name;
  private String _password;

  public UserDataPublic()
  {
  }

  public UserDataPublic(String id, String name, String password)
  {
    _id = id;
    _name = name;
    _password = password;
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

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _id + "," + _name + "]";
  }
}
