package examples.auction;

import io.baratine.core.Lookup;
import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.store.Store;

import javax.inject.Inject;

@Service("/identity-manager")
public class IdentityManagerImpl implements IdentityManager
{
  @Inject @Lookup("store:///identity")
  private Store _store;

  private long _nextId;

  @OnLoad
  public void load(Result<Boolean> result)
  {
    _store.get("/id", result.from(o -> {
      _nextId = o != null ? (Long) o : 0;
      return true;
    }));
  }

  @Override
  @Modify
  public void nextId(Result<String> result)
  {
    result.complete(Long.toString(_nextId++));
  }

  @OnSave
  public void save(Result<Boolean> result)
  {
    _store.put("/id", _nextId);

    result.complete(true);
  }
}
