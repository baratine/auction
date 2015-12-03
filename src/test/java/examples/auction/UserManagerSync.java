package examples.auction;

public interface UserManagerSync extends UserManager
{
  String create(String userName, String password, boolean isAdmin);

  String find(String name);
}
