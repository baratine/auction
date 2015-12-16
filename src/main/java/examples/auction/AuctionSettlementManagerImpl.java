package examples.auction;

import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.OnInit;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.core.Startup;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.stream.ResultStreamBuilder;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service("pod://settlement/settlement")
@Startup
@Journal()
public class AuctionSettlementManagerImpl implements AuctionSettlementManager
{
  private static Logger log
    = Logger.getLogger(AuctionSettlementManagerImpl.class.getName());

  @Inject
  @Lookup("bardb:///")
  DatabaseService _db;

  @Inject
  @Lookup("pod://auction/paypal")
  PayPal _payPal;

  @Inject
  @Lookup("pod://user/user")
  ServiceRef _userManager;

  @Inject
  @Lookup("pod://auction/auction")
  ServiceRef _auctionManager;

  @Inject
  @Lookup("pod://audit/audit")
  AuditService _auditService;

  private ServiceRef _selfRef;

  @OnInit
  public void init(Result<Boolean> result)
  {
    _selfRef = ServiceRef.current();

    Result.Fork<Boolean,Boolean> fork = result.newFork();

    _db.exec(
      "create table settlement(id varchar primary key, bid object) with hash '/settlements/$id'",
      fork.fork().from(o -> true, (e, r) -> {r.complete(true);})
    );

    _db.exec(
      "create table settlement_state(id varchar primary key, state object) with hash '/settlements/$id'",
      fork.fork().from(o -> true, (e, r) -> {r.complete(true);})
    );

    fork.join((l, r) -> load(l.get(0) && l.get(1), r));
  }

  private void load(boolean initSuccess, Result<Boolean> result)
  {
    log.log(Level.FINER, String.format("checking pending settlements"));

    /*

    ResultStreamBuilder<Cursor> r = _db.findLocal(
      "select id from settlement_state where state._commitStatus = ? or state._rollbackStatus = ?",
      AuctionSettlement.Status.SETTLING,
      AuctionSettlement.Status.ROLLING_BACK);
*/

    ResultStreamBuilder<Cursor> r = _db.findLocal(
      "select id from settlement_state");

    r.forEach(c -> resume(c)).exec();

    result.complete(true);
  }

  private void resume(Cursor cursor)
  {
    AuctionSettlement settlement
      = _selfRef.lookup('/' + cursor.getString(1)).as(AuctionSettlement.class);

    log.log(Level.FINER, String.format("resume settlement %1$s", settlement));

    settlement.getTransactionState(t -> {
      log.log(Level.FINER, String.format(
        "resume settlement %1$s with state %2$s",
        settlement,
        t));

      if (t.getRefundStatus() == AuctionSettlement.Status.ROLLING_BACK) {
        settlement.refund(Result.ignore());
      }
      else if (t.getSettleStatus() == AuctionSettlement.Status.SETTLING) {
        settlement.settleResume(Result.ignore());
      }
    });
  }

  /**
   * @param path settlement id in form of "/{id}" where {id} is a settlement id
   * @return
   */
  @OnLookup
  public Object lookup(String path)
  {
    String id = path.substring(1);

    return new AuctionSettlementImpl(id, this);
  }

  public ServiceRef getAuctionManager()
  {
    return _auctionManager;
  }

  public ServiceRef getUserManager()
  {
    return _userManager;
  }

  public PayPal getPayPal()
  {
    return _payPal;
  }

  public AuditService getAuditService()
  {
    return _auditService;
  }

  public DatabaseService getDatabase()
  {
    return _db;
  }
}
