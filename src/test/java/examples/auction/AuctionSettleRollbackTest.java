package examples.auction;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import examples.auction.mock.MockPayPal;
import examples.auction.mock.MockPayment;
import io.baratine.service.Lookup;
import io.baratine.service.Services;
import io.baratine.service.ServiceRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
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
public class AuctionSettleRollbackTest
{
  private static final Logger log
    = Logger.getLogger(AuctionSettleRollbackTest.class.getName());

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
  @Service("public:///settlement")
  ServiceRef _settlementRef;

  @Inject
  RunnerBaratine _testContext;

  @Inject
  @Service("public:///")
  Services _auctionPod;

  @Inject
  @Service("public:///paypal")
  PayPalSync _paypal;

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
      = _auctions.create(new AuctionDataInit(user.get().getId(),
                                             title,
                                             bid));

    return getAuction(id);
  }

  AuctionSync getAuction(String id)
  {
    return _auctionsRef.lookup("/" + id).as(AuctionSync.class);
  }

  AuctionSettlementSync getSettlement(AuctionSync auction)
  {
    String settlementId = auction.getSettlementId();

    AuctionSettlementSync settlement =
      _settlementRef.lookup("/" + settlementId).as(AuctionSettlementSync.class);

    return settlement;
  }

  @Test
  public void testAuctionSettle() throws InterruptedException
  {
    UserSync userSpock = createUser("Spock", "test");
    UserSync userKirk = createUser("Kirk", "test");

    AuctionSync auction = createAuction(userSpock, "book", 1);

    Assert.assertNotNull(auction);

    Assert.assertTrue(auction.open());

    Assert.assertTrue(auction.bid(new Bid(userKirk.get().getId(), 2)));

    _paypal.setPaymentResult(new MockPayment("sale-id",
                                             Payment.PaymentState.approved));

    Assert.assertTrue(auction.close());

    AuctionSettlementSync settlement = getSettlement(auction);

    AuctionSettlement.Status status = settlement.commitStatus();

    int i = 0;
    while (status == AuctionSettlement.Status.SETTLING && i++ < 10) {
      Thread.sleep(10);
      status = settlement.commitStatus();
    }

    Assert.assertEquals(AuctionSettlement.Status.SETTLED, status);

    AuctionDataPublic auctionData = auction.get();
    Assert.assertEquals(AuctionDataPublic.State.SETTLED,
                        auctionData.getState());

    UserSync winner = getUser(auctionData.getLastBidder());
    UserData winnerUserData = winner.get();

    Assert.assertTrue(winnerUserData.getWonAuctions()
                                    .contains(auctionData.getId()));

    Assert.assertEquals(auctionData.getLastBidder(), winnerUserData.getId());

    //refund
    status = settlement.rollback();

    i = 0;
    while (status == AuctionSettlement.Status.ROLLING_BACK && i < 10) {
      Thread.sleep(10);
      status = settlement.rollbackStatus();
    }

    auctionData = auction.get();

    Assert.assertEquals(AuctionDataPublic.State.ROLLED_BACK,
                        auctionData.getState());

    Assert.assertNull(auctionData.getWinner());

    Assert.assertEquals(0, winner.get().getWonAuctions().size());

    SettlementTransactionState state = settlement.getTransactionState();

    Assert.assertEquals(state.getSettleStatus(),
                        AuctionSettlement.Status.SETTLED);

    Assert.assertEquals(state.getRefundStatus(),
                        AuctionSettlement.Status.ROLLED_BACK);

    Assert.assertEquals(state.getAuctionWinnerUpdateState(),
                        SettlementTransactionState.AuctionWinnerUpdateState.SUCCESS);
    Assert.assertEquals(state.getAuctionWinnerResetState(),
                        SettlementTransactionState.AuctionWinnerUpdateState.ROLLED_BACK);

    Assert.assertEquals(state.getUserSettleState(),
                        SettlementTransactionState.UserUpdateState.SUCCESS);
    Assert.assertEquals(state.getUserResetState(),
                        SettlementTransactionState.UserUpdateState.ROLLED_BACK);

    Assert.assertEquals(state.getPaymentState(),
                        SettlementTransactionState.PaymentTxState.SUCCESS);
    Assert.assertEquals(state.getRefundState(),
                        SettlementTransactionState.PaymentTxState.REFUNDED);

  }
}
