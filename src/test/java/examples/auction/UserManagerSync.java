package examples.auction;

public interface UserManagerSync extends UserManager
{
  String createUser(String userName,
                            String password);

  String find(String name);
}
