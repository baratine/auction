package examples.auction;

import io.baratine.service.Result;

public interface UserManager
{
  void create(String userName,
              String password,
              boolean isAdmin,
              Result<String> userId);

  void find(String name, Result<String> userId);
}
