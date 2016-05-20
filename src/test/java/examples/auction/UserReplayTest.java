package examples.auction;

import javax.inject.Inject;

import com.caucho.junit.RunnerBaratine;
import io.baratine.service.Service;
import io.baratine.service.ServiceRef;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for user create() with journal replay
 */
@RunWith(RunnerBaratine.class)
public class UserReplayTest
{
  @Inject
  @Service("/User")
  UserVaultSync _userManager;

  @Inject
  @Service("/User")
  ServiceRef _userManagerRef;

  @Inject
  RunnerBaratine _testContext;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void createUser()
  {
/*    _userManager.create("Spock", "Password", false);

    _testContext.closeImmediate();
    _testContext.start();

    UserSync user = _userManagerRef.lookup("/" + id).as(UserSync.class);

    UserData userData = user.get();

    Assert.assertEquals(id, userData.getId());
    Assert.assertEquals("Spock", userData.getName());
    */
  }
}
