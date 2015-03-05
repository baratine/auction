package examples.auction;

import io.baratine.core.Result;

public interface UserManager
{
  void createUser(String userName,
                  String password,
                  Result<String> userId);

  void find(String name, Result<String> userId);
}
