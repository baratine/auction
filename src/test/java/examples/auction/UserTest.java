package examples.auction;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.caucho.junit.RunnerBaratine;
import com.caucho.junit.ServiceTest;

import io.baratine.service.ResultFuture;
import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.vault.IdAsset;

import examples.auction.AuctionSession.UserInitData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for simple App.
 */
@RunWith(RunnerBaratine.class)
@ServiceTest(UserVault.class)
public class UserTest
{
  @Inject
  @Service("/User")
  UserVaultSync _userVault;

  @Inject
  Services _services;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void createUser()
  {
    IdAsset idAsset
      = _userVault.create(new UserInitData("Spock", "Password", false));

    User user = _services.service(User.class, idAsset.toString());

    ResultFuture<UserData> userDataResult = new ResultFuture<>();

    user.get(userDataResult);

    UserData userData = userDataResult.get(1, TimeUnit.SECONDS);

    Assert.assertEquals("Spock", userData.getName());
  }

  @Test
  public void authenticateUser()
  {
    IdAsset idAsset
      = _userVault.create(new UserInitData("Spock", "Password", false));

    User user
      = Services.current().service(User.class, idAsset.toString());

    ResultFuture<Boolean> authResultAllow = new ResultFuture<>();
    user.authenticate("Password", false, authResultAllow);

    Assert.assertTrue(authResultAllow.get(1, TimeUnit.SECONDS));

    ResultFuture<Boolean> authResultReject = new ResultFuture<>();
    user.authenticate("bogus", false, authResultReject);

    Assert.assertFalse(authResultReject.get(1, TimeUnit.SECONDS));
  }

  @Test
  public void findUser()
  {
    final IdAsset idAsset
      = _userVault.create(new UserInitData("Doug", "Password", false));

    final UserSync user
      = _services.service(UserSync.class, idAsset.toString());

    Assert.assertEquals("Doug", user.get().getName());

    final User doug = _userVault.findByName("Doug");

    ResultFuture<UserData> data = new ResultFuture<>();

    doug.get(data);

    Assert.assertEquals("DVS1aMAAR3I",
                        data.get(1, TimeUnit.SECONDS).getEncodedId());

    final User bogus = _userVault.findByName("bogus");

    Assert.assertNull(bogus);
  }
}
