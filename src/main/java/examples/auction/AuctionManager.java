package examples.auction;

import io.baratine.core.Result;
import io.baratine.stream.ResultStreamBuilder;

public interface AuctionManager
{
  void create(AuctionDataInit initData, Result<String> auctionId);

  void find(String title, Result<String> auctionId);

  ResultStreamBuilder<String> search(String query);
}
