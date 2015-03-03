package examples.auction;

import io.baratine.core.Result;

public interface UserService
{
  void createUser(String userName,
                  String password,
                  Result<Boolean> result);

  void getUser(String id, Result<UserDataPublic> result);

  void authenticate(String userName,
                    String password,
                    Result<UserDataPublic> result);
}
