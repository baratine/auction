package examples.auction;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import com.caucho.junit.ServiceTest;
import com.caucho.junit.State;
import examples.auction.AuctionSession.UserInitData;
import examples.auction.mock.MockPayPal;
import io.baratine.service.ResultFuture;
import io.baratine.service.Service;
import io.baratine.service.Services;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RunWith(RunnerBaratine.class)
@ServiceTest(UserVault.class)
@ServiceTest(AuctionVault.class)
@ServiceTest(AuditServiceImpl.class)
@ServiceTest(AuctionSettlementVault.class)
@ServiceTest(MockPayPal.class)
@ConfigurationBaratine(workDir = "/tmp/baratine", testTime = ConfigurationBaratine.TEST_TIME)
public class EnsureTest
{
  @Inject @Service("/User")
  UserVault _users;

  @Inject @Service("/Auction")
  AuctionVault _auctions;

  @Inject @Service("/AuctionSettlement")
  AuctionSettlementVault _settlements;

  @Inject @Service("/PayPal")
  MockPayPal _mockPayPal;

  @Inject
  Services _services;

  @Inject
  RunnerBaratine _baratine;

  @Test
  public void testNormal()
    throws IOException, InterruptedException
  {
    UserSync spock = createUser("Spock", "passwd");
    UserSync kirk = createUser("Kirk", "passwd");

    AuctionSync auction = createAuction(spock.get().getEncodedId(), "Book", 12);

    Assert.assertTrue(auction.open());
    Assert.assertTrue(auction.bid(new AuctionBid(kirk.get().getEncodedId(),
                                                 13)));
    Assert.assertEquals(13, auction.get().getLastBid().getBid());

    auction.close();

    String spockId = spock.get().getEncodedId();
    String kirkId = kirk.get().getEncodedId();
    String auctionId = auction.get().getEncodedId();

    Auction.State state = auction.get().getState();

    int counter = 10;
    while (state != Auction.State.SETTLED && counter-- > 0) {
      state = auction.get().getState();
      State.sleep(100);
    }

    Assert.assertEquals(Auction.State.SETTLED, state);

    String settlementId = auction.getSettlementId();

    AuctionSettlementSync settlement
      = _services.service(AuctionSettlementSync.class, settlementId);

    AuctionSettlement.Status settleStatus = settlement.settleStatus();
    AuctionSettlement.Status refundStatus = settlement.refundStatus();

    Assert.assertEquals(AuctionSettlement.Status.SETTLED,
                        settleStatus);
    Assert.assertEquals(AuctionSettlement.Status.NONE,
                        refundStatus);

    _baratine.stop();

    _baratine.start();

  }

  private UserSync createUser(final String name,
                              final String passwd) throws IOException
  {
    ResultFuture<UserSync> user = new ResultFuture<>();

    _users.create(new UserInitData(name, passwd, false),
                  user.of(id -> _services.service(UserSync.class,
                                                  id.toString())));

    return user.get(1, TimeUnit.SECONDS);
  }

  private AuctionSync createAuction(String userId, String title, int bid)
  {
    ResultFuture<AuctionSync> auction = new ResultFuture<>();

    _auctions.create(new AuctionDataInit(userId, title, bid),
                     auction.of(id -> _services.service(AuctionSync.class,
                                                        id.toString())));
    return auction.get(1, TimeUnit.SECONDS);
  }

}
