package examples.auction;

import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.Modify;
import io.baratine.core.OnInit;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.core.Services;
import io.baratine.db.DatabaseService;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@Service("pod://user/user")
@Journal(count = 1)
public class UserManagerImpl implements UserManager
{
  private final static Logger log
    = Logger.getLogger(UserManagerImpl.class.getName());

  @Inject @Lookup("bardb:///")
  private DatabaseService _db;

  @Inject @Lookup("/identity-manager")
  private IdentityManager _identityManager;

  private ServiceRef _self;

  public UserManagerImpl()
  {
  }

  @OnInit
  public void onInit(Result<Boolean> result)
  {
    _self = Services.getCurrentService();

    try {
      // for production add salt
      _db.exec(
        "create table users(id varchar primary key, name varchar, value object) with hash '/user/$id'",
        result.from(o -> o != null));
    } catch (Throwable t) {
      log.log(Level.FINE, t.getMessage(), t);
      //assume that exception is due to existing table and complete with true
      result.complete(true);
    }
  }

  @OnLookup
  public Object lookup(String path)
  {
    String id = path.substring(1);

    log.finer("lookup user: " + id);

    return new UserImpl(_db, id);
  }

  @Override
  public void create(String userName,
                     String password,
                     Result<String> userId)
  {
    log.finer("create new user: " + userName);

    _identityManager.nextId(userId.from((id, r)
                                          -> createWithId(id,
                                                          userName,
                                                          password,
                                                          r)));
  }

  private void createWithId(String id,
                            String userName,
                            String password,
                            Result<String> userId)
  {
    User user = _self.lookup("/" + id).as(User.class);

    user.create(userName, password, userId);
  }

  @Override
  public void find(String name, Result<String> userId)
  {
    _self.save();

    _db.findOne("select id from users where name=?",
                userId.from(c -> c != null ? c.getString(1) : null), name);
  }
}
