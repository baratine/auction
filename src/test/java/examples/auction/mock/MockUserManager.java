package examples.auction.mock;

import examples.auction.CreditCard;
import examples.auction.User;
import examples.auction.UserData;
import examples.auction.UserManagerImpl;
import io.baratine.core.Journal;
import io.baratine.core.Result;
import io.baratine.core.Service;

@Service("pod://user/user")
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
                           Result<Boolean> result)
  {
    _user.authenticate(password, result);
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
    result.complete(false);
    //_user.addWonAuction(auction, result);
  }

  @Override
  public void removeWonAuction(String auction,
                               Result<Boolean> result)
  {
    _user.removeWonAuction(auction, result);
  }
}
