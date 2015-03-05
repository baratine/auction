package examples.auction;

import io.baratine.core.Result;

public interface UserManager
{
  void create(String userName,
              String password,
              Result<String> userId);

  void find(String name, Result<String> userId);
}
