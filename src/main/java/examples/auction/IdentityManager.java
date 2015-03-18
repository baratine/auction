package examples.auction;

import io.baratine.core.Result;

public interface IdentityManager
{
  void nextId(Result<String> result);
}
