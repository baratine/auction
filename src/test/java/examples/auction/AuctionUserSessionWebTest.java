package examples.auction;

import java.io.IOException;
import java.util.Arrays;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.HttpClient;
import com.caucho.junit.ServiceTest;
import com.caucho.junit.WebRunnerBaratine;
import examples.auction.AuctionSession.UserInitData;
import examples.auction.AuctionSession.WebAuction;
import examples.auction.AuctionSession.WebUser;
import examples.auction.AuctionUserSession.WebBid;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WebRunnerBaratine.class)
@ServiceTest(UserVault.class)
@ServiceTest(AuctionVault.class)
@ServiceTest(AuctionUserSessionImpl.class)
@ServiceTest(AuditServiceImpl.class)
@ConfigurationBaratine(workDir = "/tmp/baratine")
public class AuctionUserSessionWebTest
{
  public final static String sessionA = "aaaaa";
  public final static String sessionB = "abbbb";
  public final static String sessionC = "acccc";

  @Test
  public void testUserCreate(HttpClient client) throws IOException
  {
    WebUser user = userCreate(client, sessionA, "Spock", "passwd", false);

    Assert.assertEquals("Spock", user.getName());
  }

  @Test
  public void testUserLogin(HttpClient client) throws IOException
  {
    userCreate(client, sessionA, "Spock", "passwd", false);

    boolean isLoggedIn = userLogin(client, sessionA, "Spock", "passwd");

    Assert.assertTrue(isLoggedIn);
  }

  @Test
  public void testUserLoginFail(HttpClient client) throws IOException
  {
    userCreate(client, sessionA, "Spock", "passwd", false);

    boolean isLoggedIn = userLogin(client, sessionA, "Spock", "bogus");

    Assert.assertFalse(isLoggedIn);
  }

  @Test
  public void testAuctionCreate(HttpClient client) throws IOException
  {
    userCreate(client, sessionA, "Spock", "passwd", false);

    boolean isLoggedIn = userLogin(client, sessionA, "Spock", "passwd");

    Assert.assertTrue(isLoggedIn);

    WebAuction auction = auctionCreate(client, sessionA, "book", 15);

    Assert.assertEquals("WebAuction[book, 15, OPEN]", String.valueOf(auction));
  }

  @Test
  public void testAuctionBid(HttpClient client) throws IOException
  {
    userCreate(client, sessionA, "Spock", "passwd", false);

    boolean isLoggedIn = userLogin(client, sessionA, "Spock", "passwd");

    Assert.assertTrue(isLoggedIn);

    WebAuction auction = auctionCreate(client, sessionA, "book", 15);

    Assert.assertEquals("WebAuction[book, 15, OPEN]", String.valueOf(auction));

    userCreate(client, sessionB, "Kirk", "pass", false);
    userLogin(client, sessionB, "Kirk", "pass");

    boolean isAccepted = auctionBid(client, sessionB, auction.getId(), 17);

    Assert.assertTrue(isAccepted);
  }

  @Test
  public void testAuctionBidReject(HttpClient client) throws IOException
  {
    userCreate(client, sessionA, "Spock", "passwd", false);

    boolean isLoggedIn = userLogin(client, sessionA, "Spock", "passwd");

    Assert.assertTrue(isLoggedIn);

    WebAuction auction = auctionCreate(client, sessionA, "book", 15);

    Assert.assertEquals("WebAuction[book, 15, OPEN]", String.valueOf(auction));

    userCreate(client, sessionB, "Kirk", "pass", false);
    userLogin(client, sessionB, "Kirk", "pass");

    boolean isAccepted = auctionBid(client, sessionB, auction.getId(), 13);

    Assert.assertFalse(isAccepted);
  }

  @Test
  public void testSearchActions(HttpClient client) throws IOException
  {
    userCreate(client, sessionA, "Spock", "passwd", false);

    boolean isLoggedIn = userLogin(client, sessionA, "Spock", "passwd");

    Assert.assertTrue(isLoggedIn);

    auctionCreate(client, sessionA, "book", 15);

    final WebAuction[] books = auctionSearch(client, sessionA, "book");

    Assert.assertEquals("[WebAuction[book, 15, OPEN]]",
                        Arrays.asList(books).toString());
  }

  private WebUser userCreate(HttpClient client,
                             String session,
                             final String name,
                             final String passwd,
                             final boolean isAdmin) throws IOException
  {
    HttpClient.Response response
      = client.post("/user/createUser")
              .session(session)
              .body(new UserInitData(name, passwd, isAdmin))
              .go();

    Assert.assertEquals(200, response.status());

    WebUser user = response.readObject(WebUser.class);

    return user;
  }

  private boolean userLogin(HttpClient client,
                            String session,
                            final String name,
                            final String passwd) throws IOException
  {
    HttpClient.Response response
      = client.post("/user/login")
              .session(session)
              .body(String.format("u=%1$s&p=%2$s", name, passwd))
              .type("application/x-www-form-urlencoded")
              .go();

    Assert.assertEquals(200, response.status());

    boolean isLoggedIn = response.readObject(Boolean.class);

    return isLoggedIn;
  }

  private WebAuction auctionCreate(HttpClient client,
                                   String session,
                                   String title,
                                   int price)
    throws IOException
  {
    HttpClient.Response response
      = client.post("/user/createAuction")
              .session(session)
              .body(String.format("t=%1$s&b=%2$d", title, price))
              .type("application/x-www-form-urlencoded")
              .go();

    Assert.assertEquals(200, response.status());

    WebAuction auction = response.readObject(WebAuction.class);

    return auction;
  }

  private boolean auctionBid(HttpClient client,
                             String session,
                             String auction,
                             int bid)
    throws IOException
  {
    HttpClient.Response response
      = client.post("/user/bidAuction")
              .session(session)
              .body(new WebBid(auction, bid))
              .go();

    Assert.assertEquals(200, response.status());

    boolean isAccepted = response.readObject(Boolean.class);

    return isAccepted;
  }

  private WebAuction[] auctionSearch(HttpClient client,
                                     String session,
                                     String query)
    throws IOException
  {
    HttpClient.Response response
      = client.get("/user/searchAuctions?q=" + query)
              .session(session)
              .go();

    Assert.assertEquals(200, response.status());

    WebAuction[] auctions = response.readObject(WebAuction[].class);

    return auctions;
  }
}