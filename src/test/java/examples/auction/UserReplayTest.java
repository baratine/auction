package examples.auction;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import io.baratine.core.Lookup;
import io.baratine.core.ServiceRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Unit test for user create() with journal replay
 */
@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(
  services = {IdentityManagerImpl.class, UserManagerImpl.class}, pod = "user",
  logLevel = "FINER",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "com.caucho.config",
                                     level = "WARNING"),
          @ConfigurationBaratine.Log(name = "examples.auction",
                                     level = "FINER")},
  port = 6810,
  journalDelay = 120000)
public class UserReplayTest
{
  @Inject
  @Lookup("pod://user/user")
  UserManagerSync _userManager;

  @Inject
  @Lookup("pod://user/user")
  ServiceRef _userManagerRef;

  @Inject
  RunnerBaratine _testContext;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void createUser()
  {
    final String id = _userManager.create("Spock", "Password");

    _testContext.closeImmediate();
    _testContext.start();

    UserSync user = _userManagerRef.lookup("/" + id).as(UserSync.class);

    UserDataPublic userData = user.getUserData();

    Assert.assertEquals(id, userData.getId());
    Assert.assertEquals("Spock", userData.getName());
  }
}
