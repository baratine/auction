package examples.auction;

import java.io.IOException;

import com.caucho.junit.HttpClient;
import com.caucho.junit.ServiceTest;
import com.caucho.junit.WebRunnerBaratine;
import examples.auction.AuctionSession.UserInitData;
import examples.auction.AuctionSession.WebAuction;
import examples.auction.AuctionSession.WebUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WebRunnerBaratine.class)
@ServiceTest(UserVault.class)
@ServiceTest(AuctionVault.class)
@ServiceTest(AuctionUserSessionImpl.class)
@ServiceTest(AuditServiceImpl.class)
public class UserSessionWebTest
{
  @Test
  public void testUserCreate(HttpClient client) throws IOException
  {
    WebUser user = userCreate(client, "Spock", "passwd", false);

    Assert.assertEquals("Spock", user.getName());
  }

  @Test
  public void testUserLogin(HttpClient client) throws IOException
  {
    userCreate(client, "Spock", "passwd", false);

    boolean isLoggedIn = userLogin(client, "Spock", "passwd");

    Assert.assertTrue(isLoggedIn);
  }

  @Test
  public void testUserLoginFail(HttpClient client) throws IOException
  {
    userCreate(client, "Spock", "passwd", false);

    boolean isLoggedIn = userLogin(client, "Spock", "bogus");

    Assert.assertFalse(isLoggedIn);
  }

  @Test
  public void testAuctionCreate(HttpClient client) throws IOException
  {
    userCreate(client, "Spock", "passwd", false);

    boolean isLoggedIn = userLogin(client, "Spock", "passwd");

    Assert.assertTrue(isLoggedIn);

    WebAuction auction = auctionCreate(client, "book", 15);
    
    Assert.assertEquals("WebAuction[book, 15, OPEN]", String.valueOf(auction));
  }

  private WebUser userCreate(HttpClient client,
                             final String name,
                             final String passwd,
                             final boolean isAdmin) throws IOException
  {
    HttpClient.Response response
      = client.post("/user/createUser")
              .body(new UserInitData(name, passwd, isAdmin))
              .session("aaaaaaaaaaaaaaaaaaa")
              .go();

    Assert.assertEquals(200, response.status());

    WebUser user = response.readObject(WebUser.class);

    return user;
  }

  private boolean userLogin(HttpClient client,
                            final String name,
                            final String passwd) throws IOException
  {
    HttpClient.Response response
      = client.post("/user/login")
              .body(String.format("u=%1$s&p=%2$s", name, passwd))
              .type("application/x-www-form-urlencoded")
              .session("aaaaaaaaaaaaaaaaaaa")
              .go();

    Assert.assertEquals(200, response.status());

    boolean isLoggedIn = response.readObject(Boolean.class);

    return isLoggedIn;
  }

  public WebAuction auctionCreate(HttpClient client, String title, int price)
    throws IOException
  {
    HttpClient.Response response
      = client.post("/user/createAuction")
              .body(String.format("t=%1$s&b=%2$d", title, price))
              .type("application/x-www-form-urlencoded")
              .session("aaaaaaaaaaaaaaaaaaa")
              .go();

    Assert.assertEquals(200, response.status());

    WebAuction auction = response.readObject(WebAuction.class);

    return auction;
  }
}
