package examples.auction;

import io.baratine.service.Journal;
import io.baratine.service.Lookup;
import io.baratine.service.OnInit;
import io.baratine.service.OnLookup;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceRef;
import io.baratine.db.DatabaseService;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@Service("public:///user")
@Journal()
public class UserManagerImpl implements UserManager
{
  private final static Logger log
    = Logger.getLogger(UserManagerImpl.class.getName());

  @Inject
  @Lookup("bardb:///")
  private DatabaseService _db;

  @Inject
  @Lookup("/identity-manager")
  private IdentityManager _identityManager;

  private ServiceRef _self;

  public UserManagerImpl()
  {
  }

  @OnInit
  public void onInit(Result<Boolean> result)
  {
    _self = ServiceRef.current();

    try {
      // for production add salt
      _db.exec(
        "create table users(id varchar primary key, name varchar, value object) with hash '/user/$id'",
        result.of(o -> o != null));
    } catch (Throwable t) {
      log.log(Level.FINE, t.getMessage(), t);
      //assume that exception is due to existing table and complete with true
      result.ok(true);
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
                     boolean isAdmin,
                     Result<String> userId)
  {
    log.finer("create new user: " + userName);

    _identityManager.nextId(userId.of((id, r)
                                          -> createWithId(id,
                                                          userName,
                                                          password,
                                                          isAdmin,
                                                          r)));
  }

  private void createWithId(String id,
                            String userName,
                            String password,
                            boolean isAdmin,
                            Result<String> userId)
  {
    User user = _self.lookup("/" + id).as(User.class);

    user.create(userName, password, isAdmin, userId);
  }

  @Override
  public void find(String name, Result<String> userId)
  {
    _self.save(Result.ignore());

    _db.findOne("select id from users where name=?",
                userId.of(c -> c != null ? c.getString(1) : null), name);
  }
}
