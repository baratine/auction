package examples.auction.s1;

import examples.auction.AuctionDataPublic;
import io.baratine.core.Result;

public interface AuctionSettlement
{
  void create(String auctionId,
              String userId,
              AuctionDataPublic.Bid bid,
              Result<Boolean> result);

  void commit(Result<Status> status);

  void rollback(Result<Status> status);

  void status(Result<Status> status);
}

enum Status
{
  COMMITTED,
  PENDING,
  ROLLING_BACK,
  ROLLED_BACK
}
