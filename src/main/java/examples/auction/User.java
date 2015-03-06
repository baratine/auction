package examples.auction;

import io.baratine.core.Result;

public interface User
{
  void create(String userName, String password, Result<String> userId);

  void authenticate(String password, Result<Boolean> result);

  void getUserData(Result<UserDataPublic> user);
}
