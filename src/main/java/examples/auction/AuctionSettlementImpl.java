package examples.auction;

import io.baratine.core.Lookup;
import io.baratine.core.OnInit;
import io.baratine.core.OnLoad;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.timer.TimerService;

import javax.inject.Inject;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service("pod://auction/settlement")
public class AuctionSettlementImpl implements AuctionSettlement
{
  private final static Logger log
    = Logger.getLogger(AuctionSettlementImpl.class.getName());

  @Inject
  @Lookup("pod://auction/auction")
  ServiceRef _auctions;

  @Inject
  @Lookup("pod://user/user")
  ServiceRef _users;

  @Inject
  @Lookup("bardb:///")
  DatabaseService _db;

  @Inject
  @Lookup("bardb:///")
  ServiceRef _dbServiceRef;

  @Inject
  @Lookup("timer:///")
  TimerService _timer;

  @Inject
  @Lookup("pod://auction/paypal")
  PayPal _payPal;

  @Inject
  @Lookup("pod://audit/audit")
  AuditService _audit;

  AuctionSettlement _self;

  @OnInit
  public void init(Result<Boolean> result)
  {
    _self = ServiceRef.current().as(AuctionSettlement.class);

    Result<Boolean>[] results = result.fork(2, (x, y) -> true, a -> true);

    try {
      _db.exec(
        "create table auction_settlement (auction_id varchar primary key, settlement_id varchar)",
        results[0].from(o -> true));
    } catch (Throwable t) {
      log.log(Level.FINER, t.getMessage(), t);
      results[0].complete(true);
    }

    try {
      _db.exec(
        "create table auction_payments (auction_id varchar primary key, payment object)",
        results[1].from(o -> true));
    } catch (Throwable t) {
      log.log(Level.FINER, t.getMessage(), t);
      results[1].complete(true);
    }
  }

  @OnLoad
  public void load()
  {
    _db.findAll("select auction_id, settlement_id from auction_settlement",
                c -> load(c));

  }

  private void load(Iterable<Cursor> settlements)
  {
    for (Cursor settlement : settlements) {
      _self.settle(settlement.getString(1),
                   settlement.getString(2),
                   Result.ignore());
    }
  }

  @Override
  public void settle(String auctionId,
                     String settlementId,
                     Result<Boolean> result)
  {
    Auction auction = _auctions.lookup("/" + auctionId).as(Auction.class);

    auction.get(d -> settle(d, settlementId));

    result.complete(true);
  }

  private void settle(AuctionDataPublic auction, String settlementId)
  {
    AuctionDataPublic.Bid lastBid = auction.getLastBid();

    String userId = lastBid.getUserId();

    User user = _users.lookup("/" + userId).as(User.class);

    user.getCreditCard(cc -> settle(auction,
                                    lastBid,
                                    cc,
                                    userId,
                                    settlementId));
  }

  private void settle(AuctionDataPublic auction,
                      AuctionDataPublic.Bid bid,
                      CreditCard creditCard,
                      String userId,
                      String settlementId)
  {
    _audit.settlementAuctionWillSettle(settlementId,
                                       auction,
                                       bid,
                                       Result.ignore());

    _payPal.settle(auction,
                   bid,
                   creditCard,
                   userId,
                   settlementId,
                   p ->
                     _self.processPaymentComplete(auction.getId(),
                                                  userId,
                                                  settlementId,
                                                  p,
                                                  Result.ignore()));

  }

  public void settleAuction(String auctionId, Result<Void> result)
  {
    _audit.settlementRequestAccepted(auctionId, Result.ignore());

    _db.findOne(
      "select settlement_id from auction_settlement where auction_id = ?",
      c -> auctionClosePersist(c, auctionId, result),
      auctionId);
  }

  private void auctionClosePersist(Cursor c,
                                   String auctionId,
                                   Result<Void> result)
  {
    log.log(Level.FINER, String.format("auction close persist %1$s", c));

    if (c != null) {
      result.complete(null);
    }
    else {
      String settlementId = settlementId();
      _db.exec(
        "insert into auction_settlement (auction_id, settlement_id) values (?, ?)",
        Result.ignore(),
        auctionId,
        settlementId);

//      _dbServiceRef.save(result.from(x -> null));

      _audit.settlementRequestPersisted(auctionId,
                                        settlementId,
                                        Result.ignore());

      _self.settle(auctionId, settlementId, Result.ignore());
    }
  }

  private String settlementId()
  {
    return UUID.randomUUID().toString();
  }

  @Override
  public void processPaymentComplete(String auctionId,
                                     String userId,
                                     String settlementId,
                                     Payment payment,
                                     Result<Void> result)
  {
    _audit.settlementCompletingWithPayment(settlementId,
                                           auctionId,
                                           payment,
                                           Result.ignore());

    if (payment.getStatus() == Payment.PayPalResult.approved) {
      _db.exec("replace auction_payments (auction_id, payment) values (?,?)",
               o -> deleteAuctionSettlementRequest(auctionId),
               auctionId,
               payment);

      User user = _users.lookup("/" + userId).as(User.class);

      user.addWonAuction(auctionId, Result.ignore());

      Auction auction = _auctions.lookup("/" + auctionId).as(Auction.class);

      auction.setAuctionWinner(userId, Result.ignore());
    }
    else {
      //send to customer service
    }
  }

  private void deleteAuctionSettlementRequest(String auctionId)
  {
    _db.exec("delete from auction_settlement where auction_id = ?",
             Result.ignore(),
             auctionId);
  }
}