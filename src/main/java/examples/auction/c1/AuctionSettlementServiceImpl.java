package examples.auction.c1;

import examples.auction.AuctionDataPublic;
import io.baratine.core.Lookup;
import io.baratine.core.OnLoad;
import io.baratine.core.Result;
import io.baratine.db.DatabaseService;

import javax.inject.Inject;

public class AuctionSettlementServiceImpl
{

  @Inject
  @Lookup("bardb:///")
  DatabaseService _db;

  @OnLoad
  public void load()
  {

  }

  public void settle(String auctionId,
                     String userId,
                     AuctionDataPublic.Bid bid,
                     Result<Void> result)
  {
    _db.exec("insert into settlements (auction_id, user_id, bid) values (?,?,?)",
             result.from(o -> null), auctionId, userId, bid);
  }
}
