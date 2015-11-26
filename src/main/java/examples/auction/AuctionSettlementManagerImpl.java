package examples.auction;

import io.baratine.core.Lookup;
import io.baratine.core.OnInit;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.db.DatabaseService;

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

    Result<Boolean>[] results
      = result.fork(2, (l, r) -> r.complete(l.get(0) && l.get(1)));

    _db.exec(
      "create table settlement(id varchar primary key, auction_id varchar, user_id varchar, bid object) with hash '/settlements/$id'",
      results[0].from(o -> true, (e, r) -> {r.complete(true);})
    );

    _db.exec(
      "create table settlement_state(id varchar primary key, state object) with hash '/settlements/$id'",
      results[1].from(o -> true, (e, r) -> {r.complete(true);})
    );
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
