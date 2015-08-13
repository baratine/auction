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
 *
 */
@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(
  services = {IdentityManagerImpl.class, UserManagerImpl.class}, pod = "user",
  logLevel = "finer",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "com.caucho.config",
                                     level = "WARNING"),
          @ConfigurationBaratine.Log(name = "examples.auction",
                                     level = "FINER")},
  port = 6811,
  testTime = 0)

@ConfigurationBaratine(
  services = {IdentityManagerImpl.class, AuctionManagerImpl.class},
  pod = "auction",
  logLevel = "finer",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "FINER"),
          @ConfigurationBaratine.Log(name = "com.caucho.config",
                                     level = "WARNING"),
          @ConfigurationBaratine.Log(name = "examples.auction",
                                     level = "FINER")},
  port = 6810,
  testTime = 0)

public class AuctionReplayTest
{
  private static final Logger log
    = Logger.getLogger(AuctionReplayTest.class.getName());

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
  RunnerBaratine _testContext;

  @Inject
  @Lookup("pod://auction/")
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
    String id = _auctions.create(new AuctionDataInit(user.getUserData().getId(),
                                                     title,
                                                     bid));

    return getAuction(id);
  }

  AuctionSync getAuction(String id)
  {
    return getAuctionServiceRef(id).as(AuctionSync.class);
  }

  ServiceRef getAuctionServiceRef(String id)
  {
    return _auctionsRef.lookup("/" + id);
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

    String auctionId = auction.get().getId();

    // successful bid
    result = auction.bid(new Bid(userKirk.getUserData().getId(), 20));
    Assert.assertTrue(result);
    AuctionDataPublic data = auction.get();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.getUserData().getId());

    _testContext.closeImmediate();

    _testContext.start();

    auction = getAuction(auctionId);

    data = auction.get();
    System.out.println("AuctionReplayTest.testAuctionBid " + data);
    Assert.assertEquals(data.getLastBid().getBid(), 20);
  }
}
