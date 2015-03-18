package examples.auction;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import io.baratine.core.Lookup;
import io.baratine.core.ServiceManager;
import io.baratine.core.ServiceRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * AuctionResource unit tests.
 * <p/>
 * testTime is set to use artificial time to test auction timeouts.
 */
@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(services = {IdentityManagerImpl.class, UserManagerImpl.class}, pod = "user",
  logLevel = "finer",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction", level = "FINER")},
  testTime = 0)

@ConfigurationBaratine(services = {IdentityManagerImpl.class, AuctionManagerImpl.class}, pod = "auction",
  logLevel = "finer",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction", level = "FINER")},
  testTime = 0)

public class AuctionTest
{
  private static final Logger log
    = Logger.getLogger(AuctionTest.class.getName());

  @Inject @Lookup("pod://user/user")
  UserManagerSync _users;

  @Inject @Lookup("pod://user/user")
  ServiceRef _usersRef;

  @Inject @Lookup("pod://auction/auction")
  AuctionManagerSync _auctions;

  @Inject @Lookup("pod://auction/auction")
  ServiceRef _auctionsRef;

  @Inject
  RunnerBaratine _testContext;

  @Inject @Lookup("pod://auction/")
  ServiceManager _auctionPod;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void testAuctionCreateName()
  {
    UserSync user = createUser("Spock", "Password");

    AuctionSync auction = createAuction(user, "book", 15);
    Assert.assertNotNull(auction);

    AuctionDataPublic data = auction.getAuctionData();
    Assert.assertNotNull(data);
    Assert.assertEquals(user.getUserData().getId(),
                        auction.getAuctionData().getOwnerId());
    Assert.assertEquals(data.getTitle(), "book");
  }

  UserSync createUser(String name, String password)
  {
    String id = _users.create(name, password);

    return getUser(id);
  }

  UserSync getUser(String id)
  {
    return _usersRef.lookup("/" + id).as(UserSync.class);
  }

  AuctionSync createAuction(UserSync user, String title, int bid)
  {
    String id = _auctions.create(user.getUserData().getId(), title, bid);

    return getAuction(id);
  }

  AuctionSync getAuction(String id)
  {
    return _auctionsRef.lookup("/" + id).as(AuctionSync.class);
  }

  /**
   * open and close an auction.
   */

  @Test
  public void openClose() throws InterruptedException
  {
    UserSync user = createUser("Spock", "test");

    AuctionSync auction = createAuction(user, "book", 15);

    Assert.assertNotNull(auction);

    AuctionDataPublic data = auction.getAuctionData();
    Assert.assertEquals(AuctionDataPublic.State.INIT, data.getState());

    boolean result = auction.open();
    Assert.assertTrue(result);

    data = auction.getAuctionData();
    Assert.assertEquals(AuctionDataPublic.State.OPEN, data.getState());

    result = auction.close();
    Assert.assertTrue(result);

    data = auction.getAuctionData();
    Assert.assertEquals(AuctionDataPublic.State.CLOSED, data.getState());
  }

  /**
   * double open
   */
  @Test
  public void openOpen() throws InterruptedException
  {
    UserSync user = createUser("Spock", "test");

    AuctionSync auction = createAuction(user, "book", 15);

    Assert.assertNotNull(auction);

    boolean result = auction.open();
    Assert.assertTrue(result);

    try {
      result = auction.open();

      Assert.assertTrue(false);
    } catch (RuntimeException e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }

  /**
   * init/close
   */
  @Test
  public void initClose() throws InterruptedException
  {
    UserSync user = createUser("Spock", "test");

    AuctionSync auction = createAuction(user, "book", 15);

    Assert.assertNotNull(auction);

    try {
      auction.close();

      Assert.assertTrue(false);
    } catch (RuntimeException e) {
      e.printStackTrace();
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }

  /**
   * close open
   */
  @Test
  public void closeOpen() throws InterruptedException
  {
    UserSync user = createUser("Spock", "test");

    AuctionSync auction = createAuction(user, "book", 15);

    Assert.assertNotNull(auction);

    boolean result = auction.open();
    Assert.assertTrue(result);

    result = auction.close();
    Assert.assertTrue(result);

    try {
      auction.open();

      Assert.assertTrue(false);
    } catch (RuntimeException e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }

  /**
   * Tests normal bid.
   */

  @Test
  public void testAuctionBid() throws InterruptedException
  {
    UserSync userSpock = createUser("Spock", "test");
    UserSync userKirk = createUser("Kirk", "test");
    UserSync userUhura = createUser("Uhura", "test");

    AuctionSync auction = createAuction(userSpock, "book", 15);

    Assert.assertNotNull(auction);

    boolean result = auction.open();
    Assert.assertTrue(result);

    // successful bid
    result = auction.bid(userKirk.getUserData().getId(), 20);
    Assert.assertTrue(result);
    AuctionDataPublic data = auction.getAuctionData();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.getUserData().getId());

    // failed bid
    result = auction.bid(userUhura.getUserData().getId(), 17);
    Assert.assertFalse(result);
    data = auction.getAuctionData();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.getUserData().getId());

    result = auction.close();
    Assert.assertTrue(result);

    data = auction.getAuctionData();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.getUserData().getId());
  }

  /**
   * Tests auction events.
   */

  @Test
  public void testAuctionEvents() throws InterruptedException
  {
    UserSync userSpock = createUser("Spock", "test");
    UserSync userKirk = createUser("Kirk", "test");

    AuctionSync auction = createAuction(userSpock, "book", 15);

    Assert.assertNotNull(auction);

    auction.open();

    AuctionDataPublic data = auction.getAuctionData();
    String id = data.getId();

    String url = "event://auction/auction/" + id;

    ServiceRef eventRef = _auctionPod.lookup(url);

    System.out.println("TestAuction.testAuctionEvents: " + _auctionPod);

    AuctionListenerImpl auctionCallback = new AuctionListenerImpl("book");

    eventRef.subscribe(auctionCallback);

    auction.bid(userKirk.getUserData().getId(), 17);

    // wait for events
    Thread.sleep(100);

    Assert.assertEquals("bid", auctionCallback.getType());
    Assert.assertEquals(userKirk.getUserData().getId(),
                        auctionCallback.getUser().getUserData().getId());
    Assert.assertEquals(auctionCallback.getBid(), 17);
    Assert.assertEquals(auctionCallback.getCount(), 1);

    auction.close();

    // wait for events
    Thread.sleep(100);

    Assert.assertEquals("close", auctionCallback.getType());
    Assert.assertEquals(userKirk.getUserData().getId(),
                        auctionCallback.getUser().getUserData().getId());
    Assert.assertEquals(auctionCallback.getBid(), 17);
    Assert.assertEquals(auctionCallback.getCount(), 2);
  }

  /**
   * Tests normal auction expire (5 days)
   */

  @Test
  public void testAuctionExpire() throws InterruptedException
  {
    UserSync userSpock = createUser("Spock", "test");
    UserSync userKirk = createUser("Kirk", "test");

    AuctionSync auction = createAuction(userSpock, "book", 15);

    Assert.assertNotNull(auction);

    boolean result = auction.open();
    Assert.assertTrue(result);

    result = auction.bid(userKirk.getUserData().getId(), 20);
    Assert.assertTrue(result);

    String id = auction.getAuctionData().getId();

    String url = "event://auction/auction/" + id;
    ServiceRef eventRef = _auctionPod.lookup(url);
    AuctionListenerImpl auctionCallback = new AuctionListenerImpl("book");
    ServiceRef callbackRef = _auctionPod.service(auctionCallback);
    eventRef.subscribe(callbackRef, 0);

    // 25 seconds later auction is still open
    _testContext.addTime(15, TimeUnit.SECONDS);

    Thread.sleep(100);

    AuctionDataPublic data = auction.getAuctionData();

    Assert.assertEquals(AuctionDataPublic.State.OPEN, data.getState());

    Assert.assertEquals("", auctionCallback.getAndClear());

    // 2 hours after that, auction is closed
    _testContext.addTime(16, TimeUnit.SECONDS);
    Thread.sleep(100);

    data = auction.getAuctionData();
    Assert.assertEquals(AuctionDataPublic.State.CLOSED, data.getState());
    Assert.assertEquals("close book user=Kirk 20",
                        auctionCallback.getAndClear());

    // 24 hours after that, no extra events
    _testContext.addTime(24, TimeUnit.HOURS);
    Thread.sleep(100);
    Assert.assertEquals("", auctionCallback.getAndClear());
  }

  class AuctionListenerImpl implements AuctionEvents
  {
    private String _title;
    private String _msg = "";
    private UserSync _user;
    private int _bid;
    private String _type = "none";
    private int _count;

    AuctionListenerImpl(String title)
    {
      _title = title;
    }

    public UserSync getUser()
    {
      return _user;
    }

    public int getBid()
    {
      return _bid;
    }

    public String getType()
    {
      return _type;
    }

    public int getCount()
    {
      return _count;
    }

    public String getAndClear()
    {
      String msg = _msg;
      _msg = "";

      return msg;
    }

    @Override
    public void onBid(AuctionDataPublic data)
    {
      _user = AuctionTest.this.getUser(data.getLastBid().getUserId());
      _bid = data.getLastBid().getBid();
      _type = "bid";
      _count++;

      addMsg("bid "
             + _title
             + " user="
             + _user.getUserData().getName()
             + " "
             + _bid);
    }

    public void addMsg(String msg)
    {
      if (!_msg.equals("")) {
        _msg += "\n";
      }

      _msg += msg;
    }

    @Override
    public void onClose(AuctionDataPublic data)
    {
      _user = AuctionTest.this.getUser(data.getLastBid().getUserId());
      _bid = data.getLastBid().getBid();
      _type = "close";
      _count++;

      addMsg("close "
             + _title
             + " user="
             + _user.getUserData().getName()
             + " "
             + _bid);
    }
  }
}
