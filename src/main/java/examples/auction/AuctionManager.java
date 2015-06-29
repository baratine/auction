package examples.auction;

import io.baratine.core.Result;
import io.baratine.stream.StreamBuilder;

public interface AuctionManager
{
  void create(String ownerId, String title, int bid, Result<String> auctionId);

  void find(String title, Result<String> auctionId);

  StreamBuilder<String> search(String query);
}
