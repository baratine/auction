package examples.auction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

import io.baratine.service.Data;
import io.baratine.service.Id;
import io.baratine.service.Modify;
import io.baratine.service.Result;

@Data
public class UserImpl implements User
{
  private static final Logger log = Logger.getLogger(UserImpl.class.getName());

  private transient MessageDigest _digest;

  @Id
  private long _id;
  private UserData _user;
  private CreditCard _creditCard;

  public UserImpl()
  {
  }

  @Override
  @Modify
  public void create(String userName,
                     String password,
                     boolean isAdmin,
                     Result<Long> id)
  {
    log.finer(String.format("UserImpl: create new user: %1$s", userName));

    _user = new UserData(_id, userName, digest(password), isAdmin);

    id.ok(_id);
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

  private boolean setUser(UserData u)
  {
    log.finer("setting user: " + _id + " ->" + _user);

    _user = u;

    return _user != null;
  }

  @Override
  public void authenticate(String password,
                           boolean isAdmin,
                           Result<Boolean> result)
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
  public void addWonAuction(long auctionId, Result<Boolean> result)
  {
    _user.addWonAuction(auctionId);

    result.ok(true);
  }

  @Override
  @Modify
  public void removeWonAuction(long auctionId, Result<Boolean> result)
  {
    _user.removeWonAuction(auctionId);

    result.ok(true);
  }
}
