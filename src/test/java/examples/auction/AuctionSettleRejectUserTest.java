package examples.auction;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.LogConfig;
import com.caucho.junit.RunnerBaratine;
import com.caucho.junit.ServiceTest;
import com.caucho.junit.State;

import io.baratine.service.Result;
import io.baratine.service.ResultFuture;
import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.vault.IdAsset;

import examples.auction.AuctionSession.UserInitData;
import examples.auction.mock.MockPayPal;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RunnerBaratine.class)
@ServiceTest(AuctionSettleRejectUserTest.UserMockVault.class)
@ServiceTest(AuctionVault.class)
@ServiceTest(AuditServiceImpl.class)
@ServiceTest(AuctionSettlementVault.class)
@ServiceTest(MockPayPal.class)
@ConfigurationBaratine(workDir = "/tmp/baratine", testTime = ConfigurationBaratine.TEST_TIME)
@LogConfig("com")
public class AuctionSettleRejectUserTest
{
  @Inject @Service("/User")
  UserAbstractVault _users;

  @Inject @Service("/Auction")
  AuctionVault _auctions;

  @Inject
  Services _services;

  @Test
  public void testSettle()
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

    State.sleep(100);

    Auction.State state = auction.get().getState();

    Assert.assertEquals(Auction.State.ROLLED_BACK, state);

    String settlementId = auction.getSettlementId();

    AuctionSettlementSync settlement
      = _services.service(AuctionSettlementSync.class, settlementId);

    AuctionSettlement.Status settleStatus = settlement.settleStatus();
    AuctionSettlement.Status refundStatus = settlement.refundStatus();

    Assert.assertEquals(AuctionSettlement.Status.SETTLE_FAILED,
                        settleStatus);
    Assert.assertEquals(AuctionSettlement.Status.ROLLED_BACK,
                        refundStatus);

    SettlementTransactionState transactionState
      = settlement.getTransactionState();

    Assert.assertEquals(SettlementTransactionState.UserUpdateState.REJECTED,
                        transactionState.getUserSettleState());

    Assert.assertEquals(SettlementTransactionState.AuctionWinnerUpdateState.ROLLED_BACK,
                        transactionState.getAuctionWinnerResetState());

    Assert.assertEquals(AuctionSettlement.Status.ROLLED_BACK,
                        transactionState.getRefundStatus());
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

  public interface UserMockVault extends UserAbstractVault<UserMock>
  {
  }

  public static class UserMock extends UserImpl
  {
    @Override
    public void addWonAuction(String auctionId, Result<Boolean> result)
    {
      result.ok(false);
    }
  }
}
