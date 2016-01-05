package examples.auction;

import io.baratine.service.Modify;
import io.baratine.service.OnLoad;
import io.baratine.service.OnSave;
import io.baratine.service.Result;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

public class UserImpl implements User
{
  private static final Logger log = Logger.getLogger(UserImpl.class.getName());

  private DatabaseService _db;
  private MessageDigest _digest;

  private String _id;
  private UserData _user;
  private CreditCard _creditCard;

  public UserImpl()
  {
  }

  public UserImpl(DatabaseService db, String id)
  {
    _db = db;
    _id = id;
  }

  @Override
  @Modify
  public void create(String userName,
                     String password,
                     boolean isAdmin,
                     Result<String> userId)
  {
    _user = new UserData(_id, userName, digest(password), isAdmin);

    log.finer("creating new user: " + userName);

    _db.exec("insert into users(id, name, value) values(?,?,?)",
             userId.of(o -> _id),
             _id,
             _user.getName(),
             _user);
  }

  public String digest(String password)
  {
    try {
      if (_digest == null)
        _digest = MessageDigest.getInstance("SHA-1");

      _digest.reset();

      if (password == null)
        password = "";

      //for production add salt
      _digest.update(password.getBytes(StandardCharsets.UTF_8));

      String digest = Base64.getEncoder().encodeToString(_digest.digest());

      return digest;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @OnLoad
  public void load(Result<Boolean> result)
  {
    log.finer("loading user: " + _id + " ...");

    _db.findOne("select value from users where id=?",
                result.of(c -> setUser(c)), _id);
  }

  private boolean setUser(Cursor c)
  {
    if (c != null)
      _user = (UserData) c.getObject(1);

    log.finer("loading user: " + _id + " ->" + _user);

    return _user != null;
  }

  @OnSave
  public void save(Result<Boolean> result)
  {
    log.finer("saving user: " + _user);

    _db.exec("insert into users(id, name, value) values(?,?,?)",
             result.of(o -> (Boolean) o),
             _id,
             _user.getName(),
             _user);
  }

  @Override
  public void authenticate(String password,
                           boolean isAdmin, Result<Boolean> result)
  {
    if (_user == null) {
      result.ok(false);

      return;
    }

    boolean isAuthenticated = _user.getDigest().equals(digest(password));
    isAuthenticated &= (isAdmin == _user.isAdmin());

    result.ok(isAuthenticated);
  }

  @Override
  public void get(Result<UserData> user)
  {
    user.ok(_user);
  }

  @Override
  public void getCreditCard(Result<CreditCard> creditCard)
  {
    CreditCard cc = _creditCard;
    if (cc == null)
      cc = new CreditCard("visa", "4214020540356393", "222", 10, 2020);

    creditCard.ok(cc);
  }

  @Override
  @Modify
  public void addWonAuction(String auctionId, Result<Boolean> result)
  {
    _user.addWonAuction(auctionId);

    result.ok(true);
  }

  @Override
  @Modify
  public void removeWonAuction(String auctionId, Result<Boolean> result)
  {
    _user.removeWonAuction(auctionId);

    result.ok(true);
  }
}
