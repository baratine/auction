package examples.auction;

public interface UserSync extends User
{
  String create(String userName, String password);

  boolean authenticate(String password);

  UserDataPublic get();
}
