package examples.auction;

import java.util.logging.Logger;

import javax.inject.Inject;

import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.vault.IdAsset;

import com.caucho.junit.LogConfig;
import com.caucho.junit.RunnerBaratine;
import com.caucho.junit.ServiceTest;
import com.caucho.junit.State;

import examples.auction.mock.MockPayPal;
import examples.auction.mock.MockPayment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 */
@RunWith(RunnerBaratine.class)
@ServiceTest(UserVault.class)
@ServiceTest(AuctionVault.class)
@ServiceTest(AuditServiceImpl.class)
@ServiceTest(AuctionSettlementVault.class)
@ServiceTest(MockPayPal.class)
//@ConfigurationBaratine(workDir = "/tmp/baratine", testTime = ConfigurationBaratine.TEST_TIME)
@LogConfig("com.caucho")
public class AuctionSettleResumeTest
{
  private static final Logger log
    = Logger.getLogger(AuctionSettleResumeTest.class.getName());

  @Inject
  @Service("public:///User")
  UserVaultSync _users;

  @Inject
  @Service("public:///Auction")
  AuctionVaultSync _auctions;

  @Inject
  @Service("public:///PayPal")
  MockPayPal _paypal;

  @Inject
  Services _services;

  @Test
  public void testAuctionSettle() throws InterruptedException
  {
    UserSync userSpock = createUser("Spock", "test");
    UserSync userKirk = createUser("Kirk", "test");

    AuctionSync auction = createAuction(userSpock, "book", 1);

    Assert.assertNotNull(auction);

    Assert.assertTrue(auction.open());

    Assert.assertTrue(auction.bid(new AuctionBid(userKirk.get().getEncodedId(),
                                                 2)));

    _paypal.configure(new MockPayment("sale-id", Payment.PaymentState.pending),
                      0,
                      Result.ignore());

    Assert.assertTrue(auction.close());

    AuctionSettlementSync settlement = getSettlement(auction);

    State.sleep(100);

    AuctionSettlement.Status status = settlement.settleStatus();

    Assert.assertEquals(AuctionSettlement.Status.SETTLING, status);

    AuctionData auctionData = auction.get();

    Assert.assertEquals(Auction.State.CLOSED, auctionData.getState());

    UserSync winner = getUser(auctionData.getLastBidder());
    UserData winnerUserData = winner.get();

    Assert.assertTrue(winnerUserData.getWonAuctions()
                                    .contains(auctionData.getEncodedId()));

    Assert.assertEquals(auctionData.getLastBidder(),
                        winnerUserData.getEncodedId());

    SettlementTransactionState state = settlement.getTransactionState();

    Assert.assertEquals(state.getSettleStatus(),
                        AuctionSettlement.Status.SETTLING);

    Assert.assertEquals(state.getRefundStatus(),
                        AuctionSettlement.Status.NONE);

    Assert.assertEquals(state.getAuctionWinnerUpdateState(),
                        SettlementTransactionState.AuctionWinnerUpdateState.SUCCESS);
    Assert.assertEquals(state.getAuctionWinnerResetState(),
                        SettlementTransactionState.AuctionWinnerUpdateState.NONE);

    Assert.assertEquals(state.getUserSettleState(),
                        SettlementTransactionState.UserUpdateState.SUCCESS);
    Assert.assertEquals(state.getUserResetState(),
                        SettlementTransactionState.UserUpdateState.NONE);

    Assert.assertEquals(state.getPaymentState(),
                        SettlementTransactionState.PaymentTxState.PENDING);
    Assert.assertEquals(state.getRefundState(),
                        SettlementTransactionState.PaymentTxState.NONE);

    //retry
    _paypal.configure(
      new MockPayment("sale-id", Payment.PaymentState.approved),
      0,
      Result.ignore());

    settlement.settleResume();

    State.sleep(100);

    status = settlement.getTransactionState().getSettleStatus();

    Assert.assertEquals(AuctionSettlement.Status.SETTLED, status);

    auctionData = auction.get();
    Assert.assertEquals(Auction.State.SETTLED, auctionData.getState());

    winner = getUser(auctionData.getLastBidder());
    winnerUserData = winner.get();

    Assert.assertTrue(winnerUserData.getWonAuctions()
                                    .contains(auctionData.getEncodedId()));

    Assert.assertEquals(auctionData.getLastBidder(),
                        winnerUserData.getEncodedId());

    state = settlement.getTransactionState();

    Assert.assertEquals(state.getSettleStatus(),
                        AuctionSettlement.Status.SETTLED);

    Assert.assertEquals(state.getRefundStatus(),
                        AuctionSettlement.Status.NONE);

    Assert.assertEquals(state.getAuctionWinnerUpdateState(),
                        SettlementTransactionState.AuctionWinnerUpdateState.SUCCESS);
    Assert.assertEquals(state.getAuctionWinnerResetState(),
                        SettlementTransactionState.AuctionWinnerUpdateState.NONE);

    Assert.assertEquals(state.getUserSettleState(),
                        SettlementTransactionState.UserUpdateState.SUCCESS);
    Assert.assertEquals(state.getUserResetState(),
                        SettlementTransactionState.UserUpdateState.NONE);

    Assert.assertEquals(state.getPaymentState(),
                        SettlementTransactionState.PaymentTxState.SUCCESS);
    Assert.assertEquals(state.getRefundState(),
                        SettlementTransactionState.PaymentTxState.NONE);

  }

  UserSync createUser(String name, String password)
  {
    IdAsset id
      = _users.create(new AuctionSession.UserInitData(name, password, false));

    return getUser(id.toString());
  }

  UserSync getUser(String id)
  {
    return _services.service(UserSync.class, id);
  }

  AuctionSync createAuction(UserSync user, String title, int bid)
  {
    IdAsset id = _auctions.create(
      new AuctionDataInit(user.get().getEncodedId(), title, bid));

    return getAuction(id.toString());
  }

  AuctionSync getAuction(String id)
  {
    return _services.service(AuctionSync.class, id);
  }

  AuctionSettlementSync getSettlement(AuctionSync auction)
  {
    String id = auction.getSettlementId();

    while (id == null)
      id = auction.getSettlementId();

    System.out.println("AuctionSettleRetryTest.getSettlement " + id);
    return _services.service(AuctionSettlementSync.class, id);
  }
}
