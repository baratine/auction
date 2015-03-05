package examples.auction;

public interface SyncUserManager extends UserManager
{
  String create(String userName,
                            String password);

  String find(String name);
}
