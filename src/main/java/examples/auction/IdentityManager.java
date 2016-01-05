package examples.auction;

import io.baratine.service.Result;

public interface IdentityManager
{
  void nextId(Result<String> result);
}
