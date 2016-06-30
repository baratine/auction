package examples.auction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.logging.Logger;

import io.baratine.service.Modify;
import io.baratine.service.Result;
import io.baratine.vault.Id;
import io.baratine.vault.IdAsset;

public class UserImpl implements User
{
  private static final Logger log = Logger.getLogger(UserImpl.class.getName());

  private transient MessageDigest _digest;

  @Id
  private IdAsset _id;

  private String _encodedId;

  private String _name;

  private String _password;
  private boolean _isAdmin;

  private HashSet<String> _wonAuctions;

  public UserImpl()
  {
  }

  @Override
  @Modify
  public void create(AuctionUserSession.UserInitData userInitData,
                     Result<IdAsset> userId)
  {
    log.finer(String.format("create new user: %1$s", userInitData.getUser()));

    _name = userInitData.getUser();
    _password = digest(userInitData.getPassword());
    _isAdmin = userInitData.isAdmin();

    _encodedId = _id.toString();

    userId.ok(_id);
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

  @Override
  public void authenticate(String password,
                           boolean isAdmin,
                           Result<Boolean> result)
  {
    boolean isAuthenticated = _password.equals(digest(password));
    isAuthenticated &= (isAdmin == _isAdmin);

    result.ok(isAuthenticated);
  }

  @Override
  public void get(Result<UserData> user)
  {
    user.ok(new UserData(getEncodedId(), _name, _wonAuctions));
  }

  private String getEncodedId()
  {
    return _encodedId;
  }

  @Override
  public void getCreditCard(Result<CreditCard> creditCard)
  {
    creditCard.ok(new CreditCard("visa", "4214020540356393", "222", 10, 2020));
  }

  @Override
  @Modify
  public void addWonAuction(String auctionId, Result<Boolean> result)
  {
    if (_wonAuctions == null)
      _wonAuctions = new HashSet<>();

    _wonAuctions.add(auctionId);

    result.ok(true);
  }

  @Override
  @Modify
  public void removeWonAuction(String auctionId, Result<Boolean> result)
  {
    if (_wonAuctions == null)
      throw new IllegalStateException();

    _wonAuctions.remove(_wonAuctions);

    result.ok(true);
  }
}
