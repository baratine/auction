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
 * Unit test for simple App.
 */
@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(services = UserManagerImpl.class, pod = "user",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction", level = "FINER")})
public class TestUser
{
  @Inject @Lookup("pod://user/user")
  UserManagerSync _userManager;

  @Inject @Lookup("pod://user/user")
  ServiceRef _userManagerRef;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void testUserCreate()
  {
    String id = _userManager.createUser("Spock", "Password");

    UserSync user = _userManagerRef.lookup("/" + id).as(UserSync.class);

    UserDataPublic userData = user.get();

    Assert.assertEquals(id, userData.getId());
    Assert.assertEquals("Spock", userData.getName());
  }

  @Test
  public void authenticate()
  {
    String id = _userManager.createUser("Spock", "Password");

    UserSync user = _userManagerRef.lookup("/" + id).as(UserSync.class);

    boolean isLoggedIn = user.authenticate("Password");

    Assert.assertTrue(isLoggedIn);

    isLoggedIn = user.authenticate("bogus");
    Assert.assertFalse(isLoggedIn);
  }
}
