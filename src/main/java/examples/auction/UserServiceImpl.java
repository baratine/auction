package examples.auction;

import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.Modify;
import io.baratine.core.OnActive;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@Service("pod://user/user")
@Journal(count = 1)
public class UserServiceImpl implements UserService
{
  private final static Logger log
    = Logger.getLogger(UserServiceImpl.class.getName());

  private final List<NewUser> _newUsers = new ArrayList<>();

  @Inject @Lookup("bardb:///")
  private DatabaseService _db;

  public UserServiceImpl()
  {
  }

  @OnActive
  public boolean onInit()
  {
    try {
      _db.exec(
        "create table users(id varchar primary key, name varchar, password varchar, value object)");
    } catch (Throwable t) {
      log.log(Level.WARNING, t.getMessage(), t);
    }

    return true;
  }

  public void getUser(String id, Result<UserDataPublic> result)
  {
    _db.findOne(
      "select value from users where id=?",
      result.chain(c -> c != null ? (UserDataPublic) c.getObject(1) : null),
      id);
  }

  @Modify
  public void createUser(String userName,
                         String password,
                         Result<Boolean> result)
  {
    final String digest = digest(password);

    final String id = UUID.randomUUID().toString();

    UserDataPublic user = new UserDataPublic(id, userName);

    _newUsers.add(new NewUser(user, digest));

    result.complete(true);

    log.finer("create user: " + userName + ", users " + _newUsers.size());
  }

  public String digest(String password)
  {
    return password;//TODO calculate digest
  }

  @OnSave
  public void save()
  {
    saveImpl();
  }

  private void saveImpl()
  {
    List<NewUser> users = new ArrayList<>(_newUsers);
    _newUsers.clear();

    log.finer("saving users: " + users);

    for (NewUser newUser : users) {
      UserDataPublic user = newUser.getUser();
      String passwordDigest = newUser.getPasswordDigest();
      _db.exec("insert into users (id, name, password, value) values (?,?,?,?)",
               Result.empty(),
               user.getId(), user.getName(), passwordDigest, user);
    }
  }

  public void authenticate(String userName,
                           String password,
                           Result<UserDataPublic> result)
  {
    String digest = digest(password);

    //TODO replace with Stream
    _db.findOne(
      "select value from users where name=? and password=?",
      result.chain(c -> loadAuthenticated(c)),
      userName, digest);
  }

  public UserDataPublic loadAuthenticated(Cursor c)
  {
    UserDataPublic user = null;

    if (c != null) {
      user = (UserDataPublic) c.getObject(1);
    }

    log.finer("authenticate complete: " + user);

    return user;
  }

  static class NewUser
  {
    UserDataPublic _user;
    String _passwordDigest;

    public NewUser(UserDataPublic user, String passwordDigest)
    {
      _user = user;
      _passwordDigest = passwordDigest;
    }

    public UserDataPublic getUser()
    {
      return _user;
    }

    public String getPasswordDigest()
    {
      return _passwordDigest;
    }
  }
}
