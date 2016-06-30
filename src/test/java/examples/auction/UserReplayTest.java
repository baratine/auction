package examples.auction;

import javax.inject.Inject;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import com.caucho.junit.ServiceTest;

import io.baratine.service.Service;
import io.baratine.service.ServiceRef;
import io.baratine.service.Services;
import io.baratine.vault.IdAsset;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for user create() with journal replay
 */
@RunWith(RunnerBaratine.class)
@ServiceTest(UserVault.class)
@ConfigurationBaratine(workDir = "/tmp/baratine", testTime = ConfigurationBaratine.TEST_TIME, journalDelay = 12000)
public class UserReplayTest
{
  @Inject
  @Service("/User")
  UserVaultSync _userManager;

  @Inject
  Services _services;

  @Inject
  @Service("/User")
  ServiceRef _userManagerRef;

  @Inject
  RunnerBaratine _baratine;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void createUser()
  {
    IdAsset idAsset = _userManager.create(
      new AuctionSession.UserInitData("Spock", "Password", false));

    _baratine.stopImmediate();
    _baratine.start();

    UserSync user = _services.service(UserSync.class, idAsset.toString());

    UserData userData = user.get();

    Assert.assertEquals(idAsset.toString(), userData.getEncodedId());
    Assert.assertEquals("Spock", userData.getName());
  }
}
