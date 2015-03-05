package examples.auction;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import io.baratine.core.Lookup;
import io.baratine.core.ServiceRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * AuctionResource unit tests.
 * <p/>
 * testTime is set to use artificial time to test auction timeouts.
 */
@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(services = UserManagerImpl.class, pod = "user",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction", level = "FINER")})

@ConfigurationBaratine(services = AuctionManagerImpl.class, pod = "auction",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction", level = "FINER")})

public class AuctionTest
{
  private static final Logger log
    = Logger.getLogger(AuctionTest.class.getName());

  @Inject @Lookup("pod://user/user")
  SyncUserManager _users;

  @Inject @Lookup("pod://user/user")
  ServiceRef _usersRef;

  @Inject @Lookup("pod://auction/auction")
  SyncAuctionManager _auctions;

  @Inject @Lookup("pod://auction/auction")
  ServiceRef _auctionsRef;

  @Inject
  RunnerBaratine _testContext;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void testAuctionCreateName()
  {
    SyncUser user = createUser("Spock", "Password");

    SyncAuction auction = createAuction(user, "book", 15);
    Assert.assertNotNull(auction);

    AuctionDataPublic data = auction.get();
    Assert.assertNotNull(data);
    Assert.assertEquals(user.get().getId(), auction.get().getOwnerId());
    Assert.assertEquals(data.getTitle(), "book");
  }

  private SyncUser createUser(String name, String password)
  {
    String id = _users.create(name, password);

    return _usersRef.lookup("/" + id).as(SyncUser.class);
  }

  private SyncAuction createAuction(SyncUser user, String title, int bid)
  {
    String id = _auctions.create(user.get().getId(), title, bid);

    return _auctionsRef.lookup("/" + id).as(SyncAuction.class);
  }

/**
 * open and close an auction.
 *//*

  @Test
  public void openClose() throws InterruptedException
  {
    User user = _users.create("Spock", "test").as(User.class);

    Auction auction = _auctionManager.create(user, "book", 15)
                                     .as(Auction.class);

    Assert.assertNotNull(auction);

    AuctionDataPublic data = getAuctionDataPublic(auction);
    Assert.assertEquals(data.getState(), Auction.State.INIT);

    boolean result = open(auction);
    Assert.assertTrue(result);

    data = getAuctionDataPublic(auction);
    Assert.assertEquals(data.getState(), Auction.State.OPEN);

    result = close(auction);
    Assert.assertTrue(result);

    data = getAuctionDataPublic(auction);
    Assert.assertEquals(data.getState(), Auction.State.CLOSED);
  }

  */
/**
 * double open
 *//*

  @Test
  public void openOpen() throws InterruptedException
  {
    User user = _users.create("Spock", "test").as(User.class);

    Auction auction = _auctionManager.create(user, "book", 15)
                                     .as(Auction.class);

    Assert.assertNotNull(auction);

    boolean result = open(auction);
    Assert.assertTrue(result);

    try {
      result = open(auction);

      Assert.assertTrue(false);
    } catch (RuntimeException e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }

  */
/**
 * init/close
 *//*

  @Test
  public void initClose() throws InterruptedException
  {
    User user = _users.create("Spock", "test").as(User.class);

    Auction auction = _auctionManager.create(user, "book", 15)
                                     .as(Auction.class);

    Assert.assertNotNull(auction);

    try {
      close(auction);

      Assert.assertTrue(false);
    } catch (RuntimeException e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }

  */
/**
 * close open
 *//*

  @Test
  public void closeOpen() throws InterruptedException
  {
    User user = _users.create("Spock", "test").as(User.class);

    Auction auction = _auctionManager.create(user, "book", 15)
                                     .as(Auction.class);

    Assert.assertNotNull(auction);

    boolean result = open(auction);
    Assert.assertTrue(result);

    result = open(auction);
    Assert.assertTrue(result);

    try {
      open(auction);

      Assert.assertTrue(false);
    } catch (RuntimeException e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }

  */
/**
 * Tests normal bid.
 *//*

  @Test
  public void testAuctionBid() throws InterruptedException
  {
    User userSpock = _users.create("Spock", "test").as(User.class);
    User userKirk = _users.create("Kirk", "test").as(User.class);
    User userUhura = _users.create("Uhura", "test").as(User.class);

    Auction auction = _auctionManager.create(userSpock, "book", 15)
                                     .as(Auction.class);

    Assert.assertNotNull(auction);

    boolean result = open(auction);
    Assert.assertTrue(result);

    result = bid(auction, userKirk, 20);
    Assert.assertTrue(result);

    // successful bid
    AuctionDataPublic data = getAuctionDataPublic(auction);
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUser(), userKirk);

    // failed bid
    result = bid(auction, userUhura, 17);
    Assert.assertFalse(result);

    data = getAuctionDataPublic(auction);
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUser(), userKirk);

    result = close(auction);
    Assert.assertTrue(result);

    data = getAuctionDataPublic(auction);
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUser(), userKirk);
  }

  */
/**
 * Tests auction events.
 *//*

  @Test
  public void testAuctionEvents() throws InterruptedException
  {
    User userSpock = _users.create("Spock", "test").as(User.class);
    User userKirk = _users.create("Kirk", "test").as(User.class);

    Auction auction = _auctionManager.create(userSpock, "book", 15)
                                     .as(Auction.class);

    Assert.assertNotNull(auction);

    open(auction);

    AuctionDataPublic data = getAuctionDataPublic(auction);
    String id = data.getId();

    String url = "event:///auction/" + id;

    ServiceRef eventRef = _manager.lookup(url);

    AuctionListenerImpl actionCallback = new AuctionListenerImpl("book");

    ServiceRef callbackRef = _manager.service(actionCallback);

    eventRef.subscribe(callbackRef, 0);

    bid(auction, userKirk, 17);

    // wait for events
    Thread.sleep(100);

    Assert.assertEquals("bid", actionCallback.getType());
    Assert.assertEquals(userKirk, actionCallback.getUser());
    Assert.assertEquals(actionCallback.getBid(), 17);
    Assert.assertEquals(actionCallback.getCount(), 1);

    close(auction);

    // wait for events
    Thread.sleep(100);

    Assert.assertEquals("close", actionCallback.getType());
    Assert.assertEquals(userKirk, actionCallback.getUser());
    Assert.assertEquals(actionCallback.getBid(), 17);
    Assert.assertEquals(actionCallback.getCount(), 2);
  }

  */
/**
 * Tests normal auction expire (5 days)
 *//*

  @Test
  public void testAuctionExpire() throws InterruptedException
  {
    User userSpock = _users.create("Spock", "test").as(User.class);
    User userKirk = _users.create("Kirk", "test").as(User.class);

    Auction auction = _auctionManager.create(userSpock, "book", 15).as(
      Auction.class);

    Assert.assertNotNull(auction);

    boolean result = open(auction);
    Assert.assertTrue(result);

    result = bid(auction, userKirk, 20);
    Assert.assertTrue(result);

    String id = getAuctionDataPublic(auction).getId();

    String url = "event:///auction/" + id;
    ServiceRef eventRef = _manager.lookup(url);
    AuctionListenerImpl auctionCallback = new AuctionListenerImpl("book");
    ServiceRef callbackRef = _manager.service(auctionCallback);
    eventRef.subscribe(callbackRef, 0);

    // 4 hours later auction is still open
    _testContext.addTime(4, TimeUnit.HOURS);

    Thread.sleep(100);

    AuctionDataPublic data;
    try {
      data = getAuctionDataPublic(auction);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    Assert.assertEquals(Auction.State.OPEN, data.getState());

    Assert.assertEquals("", auctionCallback.getAndClear());

    // 2 hours after that, auction is closed
    _testContext.addTime(2, TimeUnit.HOURS);
    Thread.sleep(100);

    data = getAuctionDataPublic(auction);
    Assert.assertEquals(Auction.State.CLOSED, data.getState());
    Assert.assertEquals("close book user=Kirk 20",
                        auctionCallback.getAndClear());

    // 24 hours after that, no extra events
    _testContext.addTime(24, TimeUnit.HOURS);
    Thread.sleep(100);
    Assert.assertEquals("", auctionCallback.getAndClear());
  }

  static class AuctionListenerImpl implements AuctionEvents
  {
    private String _title;
    private String _msg = "";
    private User _user;
    private int _bid;
    private String _type = "none";
    private int _count;

    AuctionListenerImpl(String title)
    {
      _title = title;
    }

    public User getUser()
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
      _user = data.getLastBid().getUser();
      _bid = data.getLastBid().getBid();
      _type = "bid";
      _count++;

      addMsg("bid "
             + _title
             + " user="
             + getUserDataPublic(_user).getName()
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
      _user = data.getLastBid().getUser();
      _bid = data.getLastBid().getBid();
      _type = "close";
      _count++;

      addMsg("close "
             + _title
             + " user="
             + getUserDataPublic(_user).getName()
             + " "
             + _bid);
    }

    private UserDataPublic getUserDataPublic(User user)
    {
      ResultFuture<UserDataPublic> result = new ResultFuture<>();

      user.get(result);

      return result.get();
    }
  }
*/
}
