package examples.auction.s1;

import io.baratine.core.Lookup;
import io.baratine.core.OnInit;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.db.DatabaseService;

import javax.inject.Inject;

@Service("pod://settlement/settlements")
public class AuctionSettlementManagerImpl
  implements AuctionSettlementManager
{
  private ServiceRef _selfRef;

  @Inject
  @Lookup("bardb:///")
  DatabaseService _db;

  @OnInit
  public void init(Result<Boolean> result)
  {
    _selfRef = ServiceRef.current();

    Result<Boolean>[] results = result.fork(3);

    _db.exec(
      "create table settlement(id varchar primary key, auction_id varchar, user_id varchar, bid object) with hash '/settlements/$id'",
      results[0].from(o -> true, (e, r) -> {r.complete(true);})
    );

    _db.exec(
      "create table settlement_intent(id varchar primary key, intent object) with hash '/settlements/$id'",
      results[1].from(o -> true, (e, r) -> {r.complete(true);})
    );

    _db.exec(
      "create table settlement_status(id varchar primary key, status object) with hash '/settlements/$id'",
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
