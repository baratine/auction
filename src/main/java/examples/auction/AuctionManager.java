package examples.auction;

import io.baratine.core.Result;

public interface AuctionManager
{
  void create(String ownerId, String title, int bid, Result<String> auctionId);

  void find(String title, Result<String> auctionId);
}
