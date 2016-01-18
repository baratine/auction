package examples.auction;

import com.caucho.v5.data.Repository;
import com.caucho.v5.data.RepositoryImpl;
import io.baratine.db.DatabaseService;
import io.baratine.service.Journal;
import io.baratine.service.OnInit;
import io.baratine.service.OnLookup;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;
import io.baratine.service.ServiceRef;
import io.baratine.stream.ResultStreamBuilder;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 *
 */
@Service("/user")
@Journal()
public class UserManagerImpl implements UserManager
{
  private final static Logger log
    = Logger.getLogger(UserManagerImpl.class.getName());

  @Inject
  @Service("bardb:///")
  private DatabaseService _db;

  @Inject
  @Service("/identity-manager")
  private IdentityManager _identityManager;

  private ServiceRef _self;

  private Repository<UserData,String> _userRepository;

  public UserManagerImpl()
  {
  }

  @OnInit
  public void onInit(Result<Boolean> result)
  {
    _self = ServiceRef.current();

    RepositoryImpl rep = new RepositoryImpl<>(UserData.class,
                                              String.class,
                                              _db);

    ServiceManager manager = ServiceManager.current();

    _userRepository = manager.newService(rep).as(Repository.class);

    result.ok(true);
  }

  @OnLookup
  public Object lookup(String path)
  {
    String id = path.substring(1);

    log.finer("lookup user: " + id);

    return new UserImpl(_userRepository, id);
  }

  @Override
  public void create(String userName,
                     String password,
                     boolean isAdmin,
                     Result<String> userId)
  {
    log.finer("UserManagerImpl: create new user: " + userName);

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
    log.finer("UserManagerImpl: create witId 0: " + id);

    try {
      User user = _self.lookup("/" + id).as(User.class);

      log.finer("UserManagerImpl: create witId 1: " + id);
      System.out.println("UserManagerImpl.createWithId " + id);
      user.create(userName, password, isAdmin, userId);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void find(String name, Result<String> userId)
  {
    _self.save(Result.ignore());

    ResultStreamBuilder<UserData> stream
      = _userRepository.findMatch(new String[]{"name"}, new Object[]{name});

    stream.first().result(userId.of(UserData::getId));
  }
}
