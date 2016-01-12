package examples.auction;

import io.baratine.keyvalue.Store;
import io.baratine.service.Lookup;
import io.baratine.service.Modify;
import io.baratine.service.OnLoad;
import io.baratine.service.OnSave;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service("/identity-manager")
public class IdentityManagerImpl implements IdentityManager
{
  private final static Logger log =
    Logger.getLogger(IdentityManagerImpl.class.getName());

  @Inject
  ServiceManager _manager;
  @Inject
  @Service("store:///identity")
  private Store _store;
  private long _nextId;

  @OnLoad
  public void load(Result<Boolean> result)
  {
    _store.get("/id", result.of(o -> {
      _nextId = o != null ? (Long) o : 0;
      return true;
    }));
  }

  @Override
  @Modify
  public void nextId(Result<String> result)
  {
    long nextId = _nextId++;

    log.log(Level.FINER, String.format("dealing new id %1$d", nextId));

    result.ok(Long.toString(nextId));
  }

  @OnSave
  public void save(Result<Boolean> result)
  {
    _store.put("/id", _nextId);

    result.ok(true);
  }
}
