package examples.auction;

public class UserDataPublic
{
  private String _id;
  private String _name;

  public UserDataPublic()
  {
  }

  public UserDataPublic(String id, String name)
  {
    _id = id;
    _name = name;
  }

  public String getId()
  {
    return _id;
  }

  public String getName()
  {
    return _name;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _id + "," + _name + "]";
  }
}
