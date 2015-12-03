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
import java.util.logging.Logger;

/**
 * AuctionResource unit tests.
 * <p/>
 * testTime is set to use artificial time to test auction timeouts.
 */
@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(
  services = {IdentityManagerImpl.class, UserManagerImpl.class}, pod = "user",
  logLevel = "finer",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction",
                                     level = "FINER")},
  testTime = 0)

@ConfigurationBaratine(
  services = {IdentityManagerImpl.class, AuctionManagerImpl.class, MockPayPal.class},
  pod = "auction",
  logLevel = "finer",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction",
                                     level = "FINER")},
  testTime = 0)

@ConfigurationBaratine(
  services = {AuditServiceImpl.class},
  pod = "audit",
  logLevel = "finer",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction",
                                     level = "FINER")},
  testTime = 0)

@ConfigurationBaratine(
  services = {MockLuceneService.class},
  pod = "lucene",
  logLevel = "finer",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction",
                                     level = "FINER")},
  testTime = 0)

@ConfigurationBaratine(
  services = {AuctionSettlementManagerImpl.class},
  pod = "settlement",
  logLevel = "finer",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "examples.auction",
                                     level = "FINER")},
  testTime = 0)
public class AuctionSettlementTest
{
  private static final Logger log
    = Logger.getLogger(AuctionSettlementTest.class.getName());

  @Inject
  @Lookup("pod://user/user")
  UserManagerSync _users;

  @Inject
  @Lookup("pod://user/user")
  ServiceRef _usersRef;

  @Inject
  @Lookup("pod://auction/auction")
  AuctionManagerSync _auctions;

  @Inject
  @Lookup("pod://auction/auction")
  ServiceRef _auctionsRef;

  @Inject
  @Lookup("pod://settlement/settlement")
  ServiceRef _settlementRef;

  @Inject
  RunnerBaratine _testContext;

  @Inject
  @Lookup("pod://auction/")
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

    AuctionDataPublic data = auction.get();
    Assert.assertNotNull(data);
    Assert.assertEquals(user.getUserData().getId(),
                        auction.get().getOwnerId());
    Assert.assertEquals(data.getTitle(), "book");
  }

  UserSync createUser(String name, String password)
  {
    String id = _users.create(name, password, false);

    return getUser(id);
  }

  UserSync getUser(String id)
  {
    return _usersRef.lookup("/" + id).as(UserSync.class);
  }

  AuctionSync createAuction(UserSync user, String title, int bid)
  {
    String id
      = _auctions.create(new AuctionDataInit(user.getUserData().getId(),
                                             title,
                                             bid));

    return getAuction(id);
  }

  AuctionSync getAuction(String id)
  {
    return _auctionsRef.lookup("/" + id).as(AuctionSync.class);
  }

  /**
   * Tests normal bid.
   */
  @Test
  public void testAuctionSettle() throws InterruptedException
  {
    UserSync userSpock = createUser("Spock", "test");
    UserSync userKirk = createUser("Kirk", "test");

    AuctionSync auction = createAuction(userSpock, "book", 1);

    Assert.assertNotNull(auction);

    boolean result = auction.open();
    Assert.assertTrue(result);

    result = auction.bid(new Bid(userKirk.getUserData().getId(), 2));
    result = auction.close();
    Assert.assertTrue(result);

    String settlementId = auction.getSettlementId();

    AuctionSettlementSync settlement =
      _settlementRef.lookup("/" + settlementId).as(AuctionSettlementSync.class);

    AuctionSettlement.Status status = settlement.commit();

    System.out.println(status);

    AuctionDataPublic data = auction.get();

    System.out.println("xxx: " + data);
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

    AuctionDataPublic data = auction.get();
    String id = data.getId();

    String url = "event://auction/auction/" + id;

    ServiceRef eventRef = _auctionPod.lookup(url);

    System.out.println("TestAuction.testAuctionEvents: " + _auctionPod);

    AuctionListenerImpl auctionCallback = new AuctionListenerImpl("book");

    ServiceRef callabackRef
      = _auctionPod.newService().service(auctionCallback).build();

    eventRef.subscribe(callabackRef);

    auction.bid(new Bid(userKirk.getUserData().getId(), 17));

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
      _user = AuctionSettlementTest.this.getUser(data.getLastBid().getUserId());
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
      _user = AuctionSettlementTest.this.getUser(data.getLastBid().getUserId());
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

    @Override
    public void onSettled(AuctionDataPublic auctionData)
    {

    }

    @Override
    public void onRolledBack(AuctionDataPublic auctionData)
    {

    }
  }
}
