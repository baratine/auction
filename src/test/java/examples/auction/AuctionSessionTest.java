package examples.auction;

import javax.inject.Inject;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import com.caucho.junit.ServiceTest;
import examples.auction.AuctionSession.UserInitData;
import examples.auction.AuctionSession.WebAuction;
import examples.auction.AuctionSession.WebUser;
import examples.auction.AuctionUserSession.WebBid;
import io.baratine.service.Services;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The AuctionChannel is the client-visible facade to the auction system.
 * The Channel is used for encapsulation and security. The client doesn't need
 * to see the internal services directly.
 */
@RunWith(RunnerBaratine.class)
@ServiceTest(UserVault.class)
@ServiceTest(AuctionVault.class)
@ServiceTest(AuctionUserSessionImpl.class)
@ConfigurationBaratine
public class AuctionSessionTest
{
  @Inject
  Services _manager;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void userCreate()
  {
    AuctionUserSessionSync session = getSession();

    Assert.assertNotNull(session);

    final WebUser user
      = session.createUser(new UserInitData("Spock", "passwd", false));

    Assert.assertEquals("Spock", user.getName());

    final boolean isLoggedIn = session.login("Spock", "passwd");

    Assert.assertEquals(true, isLoggedIn);

    WebUser data = session.getUser();

    Assert.assertEquals("Spock", data.getName());
  }

  AuctionUserSessionSync getSession()
  {
    AuctionUserSessionSync session
      = _manager.service("session:///user/asdf")
                .as(AuctionUserSessionSync.class);

    return session;
  }

  /**
   * Login correctly sets user.
   */
  @Test
  public void userLoginReject()
  {
    AuctionUserSessionSync session = getSession();

    Assert.assertNotNull(session);

    boolean result = session.login("bogus", "bogus");

    Assert.assertFalse(result);
  }

  /**
   * creates an auction
   */
  @Test
  public void auctionCreate()
  {
    AuctionUserSessionSync session = getSession();

    Assert.assertNotNull(session);

    WebUser user
      = session.createUser(new UserInitData("Spock", "passwd", false));

    Assert.assertTrue(session.login("Spock", "passwd"));

    Assert.assertEquals("Spock", user.getName());

    WebAuction auction = session.createAuction("book", 15);

    Assert.assertEquals("WebAuction[book, 15, OPEN]", auction.toString());
  }

  /**
   * find an auction
   */
  @Test
  public void auctionFind()
  {
    // create the auction by User Spock
    AuctionUserSessionSync sessionSpok = getSession();

    sessionSpok.createUser(new UserInitData("Spock", "passwd", false));

    sessionSpok.login("Spock", "passwd");

    WebAuction newAuction = sessionSpok.createAuction("book", 15);
    Assert.assertEquals("WebAuction[book, 15, OPEN]", newAuction.toString());

    // find the auction by User Kirk
    AuctionUserSessionSync channelKirk = createUser("Kirk", "passwd");

    WebAuction auction = channelKirk.searchAuctions("book").get(0);

    Assert.assertEquals("WebAuction[book, 15, OPEN]", auction.toString());
  }

  private AuctionUserSessionSync createUser(String user, String password)
  {
    AuctionUserSessionSync session = getSession();

    session.createUser(new UserInitData(user,
                                        password,
                                        false));

    Assert.assertTrue(session.login(user, password));

    return session;
  }

  /**
   * bid on an auction
   */
  @Test
  public void auctionBid()
  {
    // create the auction by User Spock
    AuctionUserSessionSync sessionSpock = createUser("Spock", "password");

    sessionSpock.createAuction("book", 15);

    // bid on the auction by User Kirk
    AuctionUserSessionSync sessionKirk = createUser("Kirk", "password");

    WebAuction auction = sessionKirk.searchAuctions("book").get(0);

    boolean result = sessionKirk.bidAuction(new WebBid(auction.getId(), 17));
    Assert.assertTrue(result);

    auction = sessionKirk.getAuction(auction.getId());
    Assert.assertEquals("WebAuction[book, 17, OPEN]", auction.toString());
  }

  /**
   * listener from auction bid.
   *
   * @throws InterruptedException
   */
/*
  @Test
  public void auctionBidListener() throws InterruptedException
  {
    // create the auction by User Spock
    AuctionUserSessionSync sessionSpock = createUser("Spock1", "password");

    WebAuction auction = sessionSpock.createAuction("book-listener", 15);
    
    TestChannelListener listenerCreate = new TestChannelListener();

    boolean result = sessionSpock.setListener(listenerCreate);
    
    Assert.assertTrue(result);

    result = sessionSpock.addAuctionListener(id);
    Assert.assertTrue(result);

    // bid on the auction by User Kirk
    AuctionUserSessionSync sessionKirk = createUser("Kirk", "password");

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
*/

  /**
   * listener from auction bid.
   *
   * @throws InterruptedException
   */

 /* @Test
  public void auctionCompleteListener() throws InterruptedException
  {
    // create the auction by User Spock
    AuctionUserSessionSync sessionSpock2 = createUser("Spock2", "password");

    sessionSpock2.login(null, null, "password");

    String id = sessionSpock2.createAuction("book-close", 15);
    Assert.assertNotNull(id);

    TestChannelListener listenerSpock = new TestChannelListener();
    boolean result = sessionSpock2.setListener(listenerSpock);
    Assert.assertTrue(result);

    result = sessionSpock2.addAuctionListener(id);
    Assert.assertTrue(result);

    // bid on the auction by User Kirk
    AuctionUserSessionSync sessionKirk = createUser("Kirk", "password");

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

    _testContext.addTime(5, TimeUnit.SECONDS);
    Thread.sleep(100);

    Assert.assertEquals("", listenerSpock.getAndClear());

    _testContext.addTime(30, TimeUnit.SECONDS);
    Thread.sleep(100);

    Assert.assertEquals("auction-close auction=book-close user="
                        + sessionKirk.getUser().getId()
                        + " bid=17",
                        listenerSpock.getAndClear());

    sessionSpock2.logout();
  }
*/
 /* private class TestChannelListener implements ChannelListener
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
  }*/

}
