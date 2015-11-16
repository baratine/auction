package examples.auction.s1;

import io.baratine.core.Result;

public interface CommitService
{
  void commit(String auctionId, String userId, Result<Boolean> result);
}
