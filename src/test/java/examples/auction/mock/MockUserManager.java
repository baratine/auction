package examples.auction.mock;

import examples.auction.CreditCard;
import examples.auction.User;
import examples.auction.UserData;
import examples.auction.UserManagerImpl;
import io.baratine.service.Journal;
import io.baratine.service.Result;
import io.baratine.service.Service;

@Service("public:///user")
@Journal()
public class MockUserManager extends UserManagerImpl
{
  @Override
  public Object lookup(String path)
  {
    return new UserWrapper((User) super.lookup(path));
  }
}

class UserWrapper implements User
{
  private User _user;

  public UserWrapper(User user)
  {
    _user = user;
  }

  @Override
  public void create(String userName,
                     String password,
                     boolean isAdmin,
                     Result<String> userId)
  {
    _user.create(userName, password, isAdmin, userId);
  }

  @Override
  public void authenticate(String password,
                           boolean isAdmin,
                           Result<Boolean> result)
  {
    _user.authenticate(password, false, result);
  }

  @Override
  public void get(Result<UserData> user)
  {
    _user.get(user);
  }

  @Override
  public void getCreditCard(Result<CreditCard> creditCard)
  {
    _user.getCreditCard(creditCard);
  }

  @Override
  public void addWonAuction(String auction,
                            Result<Boolean> result)
  {
    result.ok(false);
    //_user.addWonAuction(auction, result);
  }

  @Override
  public void removeWonAuction(String auction,
                               Result<Boolean> result)
  {
    _user.removeWonAuction(auction, result);
  }
}
