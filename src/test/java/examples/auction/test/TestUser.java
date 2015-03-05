package examples.auction.test;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import examples.auction.UserDataPublic;
import examples.auction.UserServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for simple App.
 */
@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(services = UserServiceImpl.class, pod = "user",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction", level = "FINER")})
public class TestUser extends BaseTest
{
  /**
   * User create correctly sets the user name.
   */
  @Test
  public void testUserCreate()
  {
    boolean b = userCreate("Spock", "Password");

    Assert.assertTrue(b);

    UserDataPublic spock = userAuthenticate("Spock", "Password");

    Assert.assertNotNull(spock);

    UserDataPublic spockCopy = userGetById(spock.getId());

    Assert.assertEquals(spock.getId(), spockCopy.getId());

    UserDataPublic bogus = userAuthenticate("bogus", "bogus");

    Assert.assertNull(bogus);
  }
}
