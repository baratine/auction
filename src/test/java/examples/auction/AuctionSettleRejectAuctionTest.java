package examples.auction;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;
import examples.auction.mock.MockAuctionManager;
import examples.auction.mock.MockPayPal;
import examples.auction.mock.MockPayment;
import io.baratine.core.Lookup;
import io.baratine.core.ServiceManager;
import io.baratine.core.ServiceRef;
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
  services = {IdentityManagerImpl.class, MockAuctionManager.class, MockPayPal.class},
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
public class AuctionSettleRejectAuctionTest
{
  private static final Logger log
    = Logger.getLogger(AuctionSettleRejectAuctionTest.class.getName());

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

  @Inject
  @Lookup("pod://auction/paypal")
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
    while (status == AuctionSettlement.Status.SETTLING && i < 10) {
      Thread.sleep(10);
      status = settlement.commitStatus();
      i++;
    }

    Assert.assertEquals(AuctionSettlement.Status.SETTLE_FAILED, status);

    SettlementTransactionState txState = settlement.getTransactionState();

    Assert.assertEquals(SettlementTransactionState.UserUpdateState.SUCCESS,
                        txState.getUserSettleState());

    Assert.assertEquals(SettlementTransactionState.AuctionWinnerUpdateState.REJECTED,
                        txState.getAuctionWinnerUpdateState());

    Assert.assertEquals(SettlementTransactionState.PaymentTxState.SUCCESS,
                        txState.getPaymentState());
  }
}

