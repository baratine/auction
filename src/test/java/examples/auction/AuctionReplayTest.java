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

public class AuctionReplayTest
{
  private static final Logger log
    = Logger.getLogger(AuctionReplayTest.class.getName());

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
   * Tests normal bid.
   */

  @Test
  public void testAuctionBid() throws InterruptedException
  {
    UserSync userSpock = createUser("Spock", "test");
    UserSync userKirk = createUser("Kirk", "test");

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

    _testContext.closeImmediate();

    _testContext.start();


    data = auction.getAuctionData();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.getUserData().getId());
  }
}
