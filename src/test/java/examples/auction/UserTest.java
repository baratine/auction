package examples.auction;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import io.baratine.service.Lookup;
import io.baratine.service.ServiceRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Unit test for simple App.
 */
@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(
  services = {IdentityManagerImpl.class, UserManagerImpl.class}, pod = "user",
  logLevel = "FINER",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction",
                                     level = "FINER")})
public class UserTest
{
  @Inject
  @Lookup("public:///user")
  UserManagerSync _userManager;

  @Inject
  @Lookup("public:///user")
  ServiceRef _userManagerRef;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void createUser()
  {
    final String id = _userManager.create("Spock", "Password", false);

    UserSync user = _userManagerRef.lookup("/" + id).as(UserSync.class);

    UserData userData = user.get();

    Assert.assertEquals(id, userData.getId());
    Assert.assertEquals("Spock", userData.getName());
  }

  @Test
  public void authenticateUser()
  {
    final String id = _userManager.create("Kirk", "Password", false);

    UserSync user = _userManagerRef.lookup("/" + id).as(UserSync.class);

    boolean isLoggedIn = user.authenticate("Password");

    Assert.assertTrue(isLoggedIn);

    isLoggedIn = user.authenticate("bogus");
    Assert.assertFalse(isLoggedIn);
  }

  @Test
  public void findUser()
  {
    final String id = _userManager.create("Doug", "Password", false);

    String findId = _userManager.find("Doug");
    Assert.assertEquals(id, findId);

    findId = _userManager.find("bogus");
    Assert.assertNull(findId);
  }
}
