package examples.auction;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import examples.auction.mock.MockPayPal;
import examples.auction.mock.MockPayment;
import io.baratine.service.Lookup;
import io.baratine.service.ServiceManager;
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
public class AuctionSettleRetryTest
{
  private static final Logger log
    = Logger.getLogger(AuctionSettleRetryTest.class.getName());

  @Inject
  @Lookup("public:///user")
  UserManagerSync _users;

  @Inject
  @Lookup("public:///user")
  ServiceRef _usersRef;

  @Inject
  @Lookup("public:///auction")
  AuctionManagerSync _auctions;

  @Inject
  @Lookup("public:///auction")
  ServiceRef _auctionsRef;

  @Inject
  @Lookup("public:///settlement")
  ServiceRef _settlementRef;

  @Inject
  RunnerBaratine _testContext;

  @Inject
  @Lookup("public:///")
  ServiceManager _auctionPod;

  @Inject
  @Lookup("public:///paypal")
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
                                             Payment.PaymentState.pending));

    Assert.assertTrue(auction.close());

    AuctionSettlementSync settlement = getSettlement(auction);

    AuctionSettlement.Status status = settlement.commitStatus();

    int i = 0;
    while (status == AuctionSettlement.Status.SETTLING && i++ < 10) {
      Thread.sleep(10);
      status = settlement.commitStatus();
    }

    Assert.assertEquals(AuctionSettlement.Status.SETTLING, status);

    AuctionDataPublic auctionData = auction.get();
    Assert.assertEquals(AuctionDataPublic.State.CLOSED,
                        auctionData.getState());

    UserSync winner = getUser(auctionData.getLastBidder());
    UserData winnerUserData = winner.get();

    Assert.assertTrue(winnerUserData.getWonAuctions()
                                    .contains(auctionData.getId()));

    Assert.assertEquals(auctionData.getLastBidder(), winnerUserData.getId());

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
    _paypal.setPaymentResult(new MockPayment("sale-id",
                                             Payment.PaymentState.approved));

    settlement.commit();

    i = 0;
    while (status == AuctionSettlement.Status.SETTLING && i++ < 10) {
      Thread.sleep(10);
      status = settlement.commitStatus();
    }

    Assert.assertEquals(AuctionSettlement.Status.SETTLED, status);

    auctionData = auction.get();
    Assert.assertEquals(AuctionDataPublic.State.SETTLED,
                        auctionData.getState());

    winner = getUser(auctionData.getLastBidder());
    winnerUserData = winner.get();

    Assert.assertTrue(winnerUserData.getWonAuctions()
                                    .contains(auctionData.getId()));

    Assert.assertEquals(auctionData.getLastBidder(), winnerUserData.getId());

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
}
