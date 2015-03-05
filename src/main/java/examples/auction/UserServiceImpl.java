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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
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

  private final MessageDigest _digest;

  @Inject @Lookup("bardb:///")
  private DatabaseService _db;

  public UserServiceImpl()
  {
    try {
      _digest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @OnActive
  public boolean onInit()
  {
    try {
      // for production add salt
      _db.exec(
        "create table users(id varchar primary key, name varchar, password varchar, value object)",
        Result.empty());
    } catch (Throwable t) {
      log.log(Level.WARNING, t.getMessage(), t);
    }

    return true;
  }

  public void getUser(String id, Result<UserDataPublic> result)
  {
    _db.findOne(
      "select value from users where id=?",
      result.from(c -> c != null ? (UserDataPublic) c.getObject(1) : null),
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

    _newUsers.add(new NewUser(user, digest, result));

    log.finer("create user: " + userName + ", users " + _newUsers.size());
  }

  public String digest(String password)
  {
    _digest.reset();
    //for production add salt

    _digest.update(password.getBytes(StandardCharsets.UTF_8));

    String digest = Base64.getEncoder().encodeToString(_digest.digest());

    return digest;
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
      Result<Boolean> createResult = newUser.getResult();

      _db.findOne("select 1 from users where name=?",
                  createResult.from((c, r) -> {insert(c, newUser, r);}),
                  user.getName());

    }
  }

  public void insert(Cursor c, NewUser newUser, Result<Boolean> result)
  {
    if (c != null) {
      result.complete(false);
    }
    else {
      String passwordDigest = newUser.getPasswordDigest();

      UserDataPublic user = newUser.getUser();

      _db.exec("insert into users (id, name, password, value) values (?,?,?,?)",
               result.from(o -> true),
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
      result.from(c -> loadAuthenticated(c)),
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
    private UserDataPublic _user;
    private String _passwordDigest;
    private Result<Boolean> _result;

    public NewUser(UserDataPublic user,
                   String passwordDigest,
                   Result<Boolean> result)
    {
      _user = user;
      _passwordDigest = passwordDigest;
      _result = result;
    }

    public UserDataPublic getUser()
    {
      return _user;
    }

    public String getPasswordDigest()
    {
      return _passwordDigest;
    }

    public Result<Boolean> getResult()
    {
      return _result;
    }
  }
}
