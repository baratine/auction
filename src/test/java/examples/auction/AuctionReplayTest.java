package examples.auction;

import java.util.logging.Logger;

import javax.inject.Inject;

import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.vault.IdAsset;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import com.caucho.junit.ServiceTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(RunnerBaratine.class)
@ServiceTest(UserVault.class)
@ServiceTest(AuctionVault.class)
@ServiceTest(AuditServiceImpl.class)
@ServiceTest(AuctionSettlementVault.class)
@ConfigurationBaratine(workDir = "/tmp/baratine",
                       testTime = ConfigurationBaratine.TEST_TIME,
                       journalDelay = 12000)
public class AuctionReplayTest
{
  private static final Logger log
    = Logger.getLogger(AuctionReplayTest.class.getName());

  @Inject
  @Service("public:///user")
  UserVaultSync _users;

  @Inject
  @Service("public:///auction")
  AuctionVaultSync _auctions;

  @Inject
  RunnerBaratine _testContext;

  @Inject
  Services _services;

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

    String auctionId = auction.get().getEncodedId();

    // successful bid
    result = auction.bid(new AuctionBid(userKirk.get().getEncodedId(), 20));
    Assert.assertTrue(result);
    AuctionData data = auction.get();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
    Assert.assertEquals(data.getLastBid().getUserId(),
                        userKirk.get().getEncodedId());

    //State.sleep(10);

    _testContext.stopImmediate();

    _testContext.start();

    auction = _services.service(AuctionSync.class, auctionId);

    data = auction.get();
    Assert.assertEquals(data.getLastBid().getBid(), 20);
  }

  UserSync createUser(String name, String password)
  {
    IdAsset id = _users.create(
      new AuctionSession.UserInitData(name, password, false));

    return _services.service(UserSync.class, id.toString());
  }

  AuctionSync createAuction(UserSync user, String title, int bid)
  {
    IdAsset id = _auctions.create(
      new AuctionDataInit(user.get().getEncodedId(),
                          title,
                          bid));

    return _services.service(AuctionSync.class, id.toString());
  }
}
