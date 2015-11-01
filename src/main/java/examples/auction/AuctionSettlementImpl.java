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
import java.util.HashSet;
import java.util.Set;
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

  @Inject
  @Lookup("pod://audit/audit")
  AuditService _audit;

  AuctionSettlement _self;

  Set<String> _idepmpotencyKeys = new HashSet<>();

  @OnInit
  public void init(Result<Boolean> result)
  {
    _self = ServiceRef.current().as(AuctionSettlement.class);
    try {
      _db.exec(
        "create table auction_settlement (auction_id varchar primary key, idempotence_key varchar)",
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

  public void settleAuctions(CancelHandle event)
  {
    log.log(Level.FINER, "settle auctions timer");

    ResultStreamBuilder<Cursor> auctions
      = _db.find("select auction_id, idempotence_key from auction_settlement");

    auctions.local()
            .forEach(a -> settle(a.getString(1), a.getString(2)))
            .exec();
  }

  private void settle(String auctionId, String idempotenceKey)
  {
    if (_idepmpotencyKeys.contains(idempotenceKey))
      return;

    _idepmpotencyKeys.add(idempotenceKey);

    Auction auction = _auctions.lookup("/" + auctionId).as(Auction.class);
    auction.get(Result.from(d -> settle(d, idempotenceKey),
                            e -> log.log(Level.FINER,
                                         e.getMessage(),
                                         e)));
  }

  private void settle(AuctionDataPublic auction, String idempotenceKey)
  {
    AuctionDataPublic.Bid lastBid = auction.getLastBid();

    String userId = lastBid.getUserId();

    User user = _users.lookup("/" + userId).as(User.class);

    user.getCreditCard(Result.from(cc -> settle(auction,
                                                lastBid,
                                                cc,
                                                userId,
                                                idempotenceKey),
                                   e -> log.log(Level.FINER,
                                                e.getMessage(),
                                                e)
    ));
  }

  private void settle(AuctionDataPublic auction,
                      AuctionDataPublic.Bid bid,
                      CreditCard creditCard,
                      String userId,
                      String idempotenceKey)
  {
    _audit.settlementAuctionWillSettle(idempotenceKey,
                                       auction,
                                       bid,
                                       Result.ignore());

    _payPal.settle(auction,
                   bid,
                   creditCard,
                   userId,
                   idempotenceKey,
                   Result.from(p ->
                                 _self.processPaymentComplete(auction.getId(),
                                                              userId,
                                                              idempotenceKey,
                                                              p,
                                                              Result.ignore())
                     , e -> log.finer(e.toString())));

  }

  public void settleAuction(String auctionId, Result<Void> result)
  {
    _audit.settlementRequestAccepted(auctionId, Result.ignore());

    _db.findOne(
      "select idempotence_key from auction_settlement where auction_id = ?",
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
      String idempotenceKey = generateIdempotencyKey();
      _db.exec(
        "insert into auction_settlement (auction_id, idempotence_key) values (?, ?)",
        Result.ignore(),
        auctionId,
        idempotenceKey);

      _dbServiceRef.save(result.from(x -> null));

      _audit.settlementRequestPersisted(auctionId,
                                        idempotenceKey,
                                        Result.ignore());
    }
  }

  private String generateIdempotencyKey()
  {
    return UUID.randomUUID().toString();
  }

  @Override
  public void processPaymentComplete(String auctionId,
                                     String userId,
                                     String idempotenceKey,
                                     Payment payment,
                                     Result<Void> result)
  {
    _idepmpotencyKeys.remove(idempotenceKey);

    _audit.settlementCompletingWithPayment(idempotenceKey,
                                           auctionId,
                                           payment,
                                           Result.ignore());

    if (payment.getStatus() == Payment.PayPalResult.approved) {
      _db.exec("replace auction_payments (auction_id, payment) values (?,?)",
               Result.from(o -> deleteAuctionSettlementRequest(auctionId),
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
    _db.exec("delete from auction_settlement where auction_id = ?",
             Result.ignore(),
             auctionId);
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