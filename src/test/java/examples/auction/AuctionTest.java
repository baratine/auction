package examples.auction;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import com.caucho.junit.ServiceTest;
import com.caucho.junit.TestTime;
import examples.auction.AuctionSession.UserInitData;
import io.baratine.event.Events;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.vault.IdAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * AuctionResource unit tests.
 * <p/>
 * testTime is set to use artificial time to test auction timeouts.
 */
@RunWith(RunnerBaratine.class)
@ServiceTest(UserVault.class)
@ServiceTest(AuctionVault.class)
@ConfigurationBaratine
public class AuctionTest
{
  private static final Logger log
    = Logger.getLogger(AuctionTest.class.getName());

  @Inject
  @Service("/User")
  UserVaultSync _users;

  @Inject
  @Service("/Auction")
  AuctionVaultSync _auctions;

  @Inject
  @Service("event:")
  Events _events;

  @Inject
  Services _manager;

  /**
   * User create correctly sets the user name.
   */
  @Test
  public void testAuctionCreateName()
  {
    UserSync user = createUser("Spock", "Password");

    AuctionSync auction = createAuction(user, "book", 15);
    Assert.assertNotNull(auction);

    AuctionData data = auction.get();
    Assert.assertNotNull(data);
    Assert.assertEquals(user.get().getEncodedId(),
                        auction.get().getOwnerId());
    Assert.assertEquals(data.getTitle(), "book");
  }

  UserSync createUser(String name, String password)
  {
    IdAsset id
      = _users.create(new UserInitData(name, password, false));

    return getUser(id.toString());
  }

  UserSync getUser(String id)
  {
    return _manager.service(UserSync.class, id);
  }

  AuctionSync createAuction(UserSync user, String title, int bid)
  {
    IdAsset id
      = _auctions.create(new AuctionDataInit(user.get().getEncodedId(),
                                             title,
                                             bid));

    return getAuction(id.toString());
  }

  AuctionSync getAuction(String id)
  {
    return _manager.service(AuctionSync.class, id);
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

    AuctionData data = auction.get();
    Assert.assertEquals(Auction.State.INIT, data.getState());

    boolean result = auction.open();
    Assert.assertTrue(result);

    data = auction.get();
    Assert.assertEquals(Auction.State.OPEN, data.getState());

    result = auction.close();
    Assert.assertTrue(result);

    data = auction.get();
    Assert.assertEquals(Auction.State.CLOSED, data.getState());
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
      auction.open();

      Assert.assertTrue(false);
    } catch (RuntimeException e) {
      Assert.assertEquals(e.getCause().getClass(), IllegalStateException.class);
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
    } catch (Throwable t) {
      Assert.assertEquals(t.getCause().getClass(), IllegalStateException.class);
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
      Assert.assertEquals(e.getCause().getClass(), IllegalStateException.class);
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
    result = auction.bid(new AuctionBid(userKirk.get().getEncodedId(), 20));
    Assert.assertTrue(result);
    AuctionData data = auction.get();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.get().getEncodedId());

    // failed bid
    result = auction.bid(new AuctionBid(userUhura.get().getEncodedId(), 17));
    Assert.assertFalse(result);
    data = auction.get();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.get().getEncodedId());

    result = auction.close();
    Assert.assertTrue(result);

    data = auction.get();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.get().getEncodedId());
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

    AuctionData data = auction.get();

    AuctionListenerImpl auctionCallback = new AuctionListenerImpl("book");

    _events.consumer(data.getEncodedId(), auctionCallback, Result.ignore());

    auction.bid(new AuctionBid(userKirk.get().getEncodedId(), 17));

    // wait for events
    Thread.sleep(100);

    Assert.assertEquals("bid", auctionCallback.getType());
    Assert.assertEquals(userKirk.get().getEncodedId(),
                        auctionCallback.getUser().get().getEncodedId());
    Assert.assertEquals(auctionCallback.getBid(), 17);
    Assert.assertEquals(auctionCallback.getCount(), 1);

    auction.close();

    // wait for events
    Thread.sleep(100);

    Assert.assertEquals("close", auctionCallback.getType());
    Assert.assertEquals(userKirk.get().getEncodedId(),
                        auctionCallback.getUser().get().getEncodedId());
    Assert.assertEquals(auctionCallback.getBid(), 17);
    Assert.assertEquals(auctionCallback.getCount(), 2);
  }

  /**
   * Tests normal auction expire (15 seconds)
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

    result = auction.bid(new AuctionBid(userKirk.get().getEncodedId(), 20));
    Assert.assertTrue(result);

    AuctionListenerImpl auctionCallback = new AuctionListenerImpl("book");

    _events.consumer(auction.get().getEncodedId(),
                     auctionCallback,
                     Result.ignore());

    // 1 seconds later auction is still open
    TestTime.addTime(1, TimeUnit.SECONDS);

    Thread.sleep(100);

    AuctionData data = auction.get();

    Assert.assertEquals(Auction.State.OPEN, data.getState());

    Assert.assertEquals("", auctionCallback.getAndClear());

    // 30 seconds after that, auction is closed
    TestTime.addTime(30, TimeUnit.SECONDS);
    Thread.sleep(100);

    data = auction.get();
    Assert.assertEquals(Auction.State.CLOSED, data.getState());
    Assert.assertEquals("close book user=Kirk 20",
                        auctionCallback.getAndClear());

    // 24 hours after that, no extra events
    TestTime.addTime(24, TimeUnit.HOURS);
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
    public void onBid(AuctionData data)
    {
      _user = AuctionTest.this.getUser(data.getLastBid().getUserId());
      _bid = data.getLastBid().getBid();
      _type = "bid";
      _count++;

      addMsg("bid "
             + _title
             + " user="
             + _user.get().getName()
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
    public void onClose(AuctionData data)
    {
      _user = AuctionTest.this.getUser(data.getLastBid().getUserId());
      _bid = data.getLastBid().getBid();
      _type = "close";
      _count++;

      addMsg("close "
             + _title
             + " user="
             + _user.get().getName()
             + " "
             + _bid);
    }

    @Override
    public void onSettled(AuctionData auctionData)
    {

    }

    @Override
    public void onRolledBack(AuctionData auctionData)
    {

    }
  }
}
