package examples.auction;

import java.util.logging.Logger;

import javax.inject.Inject;

import com.caucho.junit.RunnerBaratine;
import io.baratine.service.Service;
import io.baratine.service.ServiceRef;
import io.baratine.service.Services;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(RunnerBaratine.class)
public class AuctionReplayTest
{
  private static final Logger log
    = Logger.getLogger(AuctionReplayTest.class.getName());

  @Inject
  @Service("public:///user")
  UserVaultSync _users;

  @Inject
  @Service("public:///user")
  ServiceRef _usersRef;

  @Inject
  @Service("public:///auction")
  AuctionVaultSync _auctions;

  @Inject
  @Service("public:///auction")
  ServiceRef _auctionsRef;

  @Inject
  RunnerBaratine _testContext;

  @Inject
  @Service("public:///")
  Services _auctionPod;

  UserSync createUser(String name, String password)
  {
    String id = _users.create(name, password, false);

    return getUser(id);
  }

  UserSync getUser(String id)
  {
    return _usersRef.service("/" + id).as(UserSync.class);
  }

  AuctionSync createAuction(UserSync user, String title, int bid)
  {
    String id = _auctions.create(new AuctionDataInit(user.get().getId(),
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
    result = auction.bid(new Bid(userKirk.get().getId(), 20));
    Assert.assertTrue(result);
    AuctionDataPublic data = auction.get();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.get().getId());

    _testContext.closeImmediate();

    _testContext.start();

    auction = getAuction(auctionId);

    data = auction.get();
    System.out.println("AuctionReplayTest.testAuctionBid " + data);
    Assert.assertEquals(data.getLastBid().getBid(), 20);
  }
}
