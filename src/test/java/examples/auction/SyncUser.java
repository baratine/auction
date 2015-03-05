package examples.auction;

public interface SyncUser extends User
{
  String create(String userName, String password);

  boolean authenticate(String password);

  UserDataPublic get();
}
