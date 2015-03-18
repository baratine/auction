package examples.auction;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import io.baratine.core.Lookup;
import io.baratine.core.ServiceManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * The AuctionChannel is the client-visible facade to the auction system.
 * The Channel is used for encapsulation and security. The client doesn't need
 * to see the internal services directly.
 */
@RunWith(RunnerBaratine.class)

@ConfigurationBaratine(services = {IdentityManagerImpl.class, UserManagerImpl.class}, pod = "user",
  logLevel = "FINER",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction", level = "FINER")},
  port = 8085,
  testTime = 0)
@ConfigurationBaratine(services = {IdentityManagerImpl.class, AuctionManagerImpl.class}, pod = "auction",
  logLevel = "FINER",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction", level = "FINER")},
  testTime = 0)
@ConfigurationBaratine(services = AuctionSessionImpl.class, pod = "web",
  logLevel = "FINER",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction", level = "FINER")},
  testTime = 0)
public class AuctionSessionTest
{
  @Inject @Lookup("pod://web/")
  ServiceManager _auctionPod;

  @Inject RunnerBaratine _testContext;

  /**
   * User create correctly sets the user name.
   */

  @Test
  public void userCreate()
  {
    AuctionSessionSync session = getSession();

    Assert.assertNotNull(session);

    boolean result = session.createUser("Spock", "passwd");

    Assert.assertTrue(result);

    session.login("Spock", "passwd");

    UserDataPublic data = session.getUser();

    Assert.assertEquals("Spock", data.getName());
  }

  AuctionSessionSync getSession()
  {
    AuctionSessionSync session = _auctionPod.lookup(
      "session://web/auction-session/test").as(AuctionSessionSync.class);

    return session;
  }

  /**
   * Login correctly sets user.
   */
  @Test
  public void userLogin()
  {
    AuctionSessionSync session = getSession();

    Assert.assertNotNull(session);

    boolean result = session.createUser("Spock", "passwd");
    Assert.assertTrue(result);

    session = getSession();

    Assert.assertNotNull(session);

    result = session.login("Spock", "passwd");
    Assert.assertTrue(result);

    UserDataPublic data = session.getUser();

    Assert.assertEquals("Spock", data.getName());
  }

  /**
   * Login correctly sets user.
   */
  @Test
  public void userLoginReject()
  {
    AuctionSessionSync session = getSession();

    Assert.assertNotNull(session);

    boolean result = session.login("bogus", null);
    Assert.assertFalse(result);

    UserDataPublic user = session.getUser();
  }

  /**
   * creates an auction
   */
  @Test
  public void auctionCreate()
  {
    AuctionSessionSync session = getSession();

    Assert.assertNotNull(session);

    boolean result = session.createUser("Spock", "passwd");
    Assert.assertTrue(result);

    String id = session.createAuction("book", 15);
    Assert.assertNotNull(id);

    AuctionDataPublic data = session.getAuction(id);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getTitle(), "book");
    Assert.assertNull(data.getLastBid());
  }

  /**
   * find an auction
   */
  @Test
  public void auctionFind()
  {
    // create the auction by User Spock
    AuctionSessionSync sessionSpok = getSession();

    boolean result = sessionSpok.createUser("Spock", "passwd");
    Assert.assertTrue(result);

    String id = sessionSpok.createAuction("book", 15);
    Assert.assertNotNull(id);

    // find the auction by User Kirk
    AuctionSessionSync channelKirk = getSession();

    result = channelKirk.createUser("Kirk", "passwd");
    Assert.assertTrue(result);

    String idFind = channelKirk.findAuction("book");
    Assert.assertNotNull(idFind);

    AuctionDataPublic data = sessionSpok.getAuction(idFind);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getTitle(), "book");
    Assert.assertNull(data.getLastBid());
  }

  /**
   * bid on an auction
   */
  @Test
  public void auctionBid()
  {
    // create the auction by User Spock
    AuctionSessionSync sessionSpock = createUser("Spock", "password");

    String id = sessionSpock.createAuction("book", 15);
    Assert.assertNotNull(id);

    // bid on the auction by User Kirk
    AuctionSessionSync sessionKirk = createUser("Kirk", "password");

    String idBid = sessionKirk.findAuction("book");
    Assert.assertNotNull(idBid);

    boolean result = sessionKirk.bidAuction(idBid, 17);
    Assert.assertTrue(result);

    AuctionDataPublic data = sessionKirk.getAuction(idBid);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getLastBid().getBid(), 17);
  }

  private AuctionSessionSync createUser(String user, String password)
  {
    AuctionSessionSync session = getSession();

    boolean result = session.createUser(user, password);

    Assert.assertTrue(result);

    return session;
  }

  /**
   * listener from auction bid.
   *
   * @throws InterruptedException
   */
  @Test
  public void auctionBidListener() throws InterruptedException
  {
    // create the auction by User Spock
    AuctionSessionSync sessionSpock = createUser("Spock1", "password");

    Assert.assertTrue(sessionSpock.login("Spock1", "password"));

    String id = sessionSpock.createAuction("book-listener", 15);
    Assert.assertNotNull(id);

    TestChannelListener listenerCreate = new TestChannelListener();

    boolean result = sessionSpock.setListener(listenerCreate);
    Assert.assertTrue(result);

    result = sessionSpock.addAuctionListener(id);
    Assert.assertTrue(result);

    // bid on the auction by User Kirk
    AuctionSessionSync sessionKirk = createUser("Kirk", "password");

    String idBid = sessionKirk.findAuction("book-listener");
    Assert.assertNotNull(idBid);

    // check callback state before bid
    Assert.assertEquals("", listenerCreate.getAndClear());

    result = sessionKirk.bidAuction(idBid, 17);
    Assert.assertTrue(result);

    AuctionDataPublic data = sessionKirk.getAuction(idBid);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getLastBid().getBid(), 17);

    // sleep because events are async
    Thread.sleep(100);

    Assert.assertEquals("auction-update auction=book-listener user="
                        + sessionKirk.getUser().getId()
                        + " bid=17",
                        listenerCreate.getAndClear());

    sessionSpock.logout();
  }

  /**
   * listener from auction bid.
   *
   * @throws InterruptedException
   */

  @Test
  public void auctionCompleteListener() throws InterruptedException
  {
    // create the auction by User Spock
    AuctionSessionSync sessionSpock2 = createUser("Spock2", "password");

    sessionSpock2.login("Spock2", "password");

    String id = sessionSpock2.createAuction("book-close", 15);
    Assert.assertNotNull(id);

    TestChannelListener listenerSpock = new TestChannelListener();
    boolean result = sessionSpock2.setListener(listenerSpock);
    Assert.assertTrue(result);

    result = sessionSpock2.addAuctionListener(id);
    Assert.assertTrue(result);

    // bid on the auction by User Kirk
    AuctionSessionSync sessionKirk = createUser("Kirk", "password");

    String idBid = sessionKirk.findAuction("book-close");
    Assert.assertNotNull(idBid);

    // check callback state before bid
    Assert.assertEquals("", listenerSpock.getAndClear());

    result = sessionKirk.bidAuction(idBid, 17);
    Assert.assertTrue(result);

    AuctionDataPublic data = sessionKirk.getAuction(idBid);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getLastBid().getBid(), 17);

    // sleep because events are async
    Thread.sleep(100);

    Assert.assertEquals("auction-update auction=book-close user="
                        + sessionKirk.getUser().getId()
                        + " bid=17",
                        listenerSpock.getAndClear());

    _testContext.addTime(10, TimeUnit.SECONDS);
    Thread.sleep(100);

    Assert.assertEquals("", listenerSpock.getAndClear());

    _testContext.addTime(2, TimeUnit.SECONDS);
    Thread.sleep(100);

    Assert.assertEquals("auction-close auction=book-close user="
                        + sessionKirk.getUser().getId()
                        + " bid=17",
                        listenerSpock.getAndClear());

    sessionSpock2.logout();
  }

  private class TestChannelListener implements ChannelListener
  {
    private String _msg = "";

    @Override
    public void onAuctionUpdate(AuctionDataPublic data)
    {
      if (!"".equals(_msg)) {
        _msg += "\n";
      }

      AuctionDataPublic.Bid bid = data.getLastBid();

      if (bid != null) {
        _msg += "auction-update auction="
                + data.getTitle()
                + " user="
                + bid.getUserId()
                + " bid="
                + bid.getBid();
      }
      else {
        _msg += "auction-update auction=" + data.getTitle() + " " + bid;
      }
    }

    @Override
    public void onAuctionClose(AuctionDataPublic data)
    {
      if (!"".equals(_msg)) {
        _msg += "\n";
      }

      AuctionDataPublic.Bid bid = data.getLastBid();

      if (bid != null) {
        _msg += "auction-close auction="
                + data.getTitle()
                + " user="
                + bid.getUserId()
                + " bid="
                + bid.getBid();
      }
      else {
        _msg += "auction-close auction=" + data.getTitle() + " " + bid;
      }
    }

    public String getAuctionData()
    {
      return _msg;
    }

    public void clear()
    {
      _msg = "";
    }

    public String getAndClear()
    {
      String msg = _msg;
      _msg = "";

      return msg;
    }
  }

}
