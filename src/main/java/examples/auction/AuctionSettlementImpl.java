package examples.auction;

import io.baratine.core.CancelHandle;
import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.OnInit;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.stream.ResultStreamBuilder;
import io.baratine.timer.TimerScheduler;
import io.baratine.timer.TimerService;

import javax.inject.Inject;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service("pod://auction/settlement")
@Journal
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

  AuctionSettlement _self;

  @OnInit
  public void init(Result<Boolean> result)
  {
    _self = ServiceRef.current().as(AuctionSettlement.class);
    try {
      _db.exec(
        "create table auction_settlement (auction_id varchar primary key, idempotency_key varchar)",
        result.from(o -> o != null));

    } catch (Throwable t) {
      log.log(Level.FINER, t.getMessage(), t);

      result.complete(true);
    }

    try {
      _db.exec(
        "create table auction_payments (auction_id varchar primary key, payment object)",
        result.from(o -> o != null));

    } catch (Throwable t) {
      log.log(Level.FINER, t.getMessage(), t);

      result.complete(true);
    }

    _timer.schedule(event -> settleAuctions(event),
                    new AuctionSettlementTimerScheduler(),
                    Result.ignore());
  }

  public void auctionClosed(String auctionId, Result<Void> result)
  {
    _db.findOne(
      "select idempotency_key from auction_settlement where auction_id = ?",
      c -> auctionClosePersist(c, auctionId, result),
      auctionId);
  }

  @Override
  public void processPaymentComplete(String auctionId,
                                     String userId,
                                     String idempotencyKey,
                                     Payment payment,
                                     Result<Void> result)
  {
    if (payment.getStatus() == Payment.PayPalResult.approved) {
      _db.exec("replace auction_payments (auction_id, payment) values (?,?)",
               Result.make(o -> deleteAuctionSettlementRequest(auctionId),
                           e -> log.log(Level.FINER, e.getMessage(), e)),
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
    _db.exec("delete auction_settlement where auction_id = ?",
             Result.ignore(),
             auctionId);
  }

  private void auctionClosePersist(Cursor c,
                                   String auctionId,
                                   Result<Void> result)
  {
    if (c != null) {
      result.complete(null);
    }
    else {
      String invNum = generateIdempotencyKey();
      _db.exec(
        "insert into auction_settlement (auction_id, idempotency_key) values (?, ?)",
        Result.ignore(),
        auctionId,
        invNum);

      _dbServiceRef.save(result.from(x -> null));
    }
  }

  private void settleAuctions(CancelHandle event)
  {
    ResultStreamBuilder<Cursor> auctions
      = _db.find("select auction_id, idempotency_key from auction_settlement");

    auctions.local()
            .forEach(a -> settle(a.getString(1), a.getString(2)))
            .exec();
  }

  private void settle(String auctionId, String idempotencyKey)
  {
    Auction auction = _auctions.lookup("/" + auctionId).as(Auction.class);
    auction.get(Result.make(d -> {settle(d, idempotencyKey);},
                            e -> log.log(Level.FINER,
                                         e.getMessage(),
                                         e)));
  }

  private void settle(AuctionDataPublic auction, String idempotencyKey)
  {
    String userId = auction.getLastBid().getUserId();

    User user = _users.lookup("/" + userId).as(User.class);

    user.getCreditCard(Result.make(cc -> settle(auction,
                                                cc,
                                                userId,
                                                idempotencyKey),
                                   e -> log.log(Level.FINER,
                                                e.getMessage(),
                                                e)
    ));
  }

  private void settle(AuctionDataPublic auction,
                      CreditCard creditCard,
                      String userId,
                      String idempotencyKey)
  {
    _payPal.settle(auction,
                   creditCard,
                   userId,
                   idempotencyKey,
                   Result.make(p ->
                                 _self.processPaymentComplete(auction.getId(),
                                                              userId,
                                                              idempotencyKey,
                                                              p,
                                                              Result.ignore())
                     , e -> log.finer(e.toString())));

  }

  private String generateIdempotencyKey()
  {
    return UUID.randomUUID().toString();
  }
}

class AuctionSettlementTimerScheduler implements TimerScheduler
{
  @Override
  public long nextRunTime(long l)
  {
    return l + 1000;
  }
}