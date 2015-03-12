package examples.auction;

public interface UserManagerSync extends UserManager
{
  String create(String userName, String password);

  String find(String name);
}
