package examples.auction;

import io.baratine.core.Lookup;
import io.baratine.core.OnInit;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.stream.ResultStreamBuilder;

import javax.inject.Inject;

@Service("pod://settlement/settlement")
public class AuctionSettlementManagerImpl
  implements AuctionSettlementManager
{
  @Inject
  @Lookup("bardb:///")
  DatabaseService _db;

  private ServiceRef _selfRef;

  @OnInit
  public void init(Result<Boolean> result)
  {
    _selfRef = ServiceRef.current();

    Result.Fork<Boolean,Boolean> fork = result.newFork();

    fork.fail((l, e, r) -> {
      for (Throwable t : e) {
        if (t != null) {
          r.fail(t);
          break;
        }
      }
    });

    _db.exec(
      "create table settlement(id varchar primary key, auction_id varchar, user_id varchar, bid object) with hash '/settlements/$id'",
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
    ResultStreamBuilder<Cursor> r = _db.findLocal(
      "select id from settlement_state where state._commitStatus = ? or state._rollbackStatus = ?",
      AuctionSettlement.Status.COMMITTING,
      AuctionSettlement.Status.ROLLING_BACK);

    r.forEach(c -> resume(c));

    result.complete(true);
  }

  private void resume(Cursor cursor)
  {
    AuctionSettlement settlement
      = _selfRef.lookup('/' + cursor.getString(1)).as(AuctionSettlement.class);

    settlement.getTransactionState(t -> {

      if (t.getRollbackStatus() == AuctionSettlement.Status.ROLLING_BACK) {
        settlement.rollback(Result.ignore());
      }
      else if (t.getCommitStatus() == AuctionSettlement.Status.COMMITTING) {
        settlement.commit(Result.ignore());
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

    return new AuctionSettlementImpl(id);
  }
}
