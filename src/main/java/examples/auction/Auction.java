package examples.auction;

import io.baratine.core.Result;

public interface Auction
{
  void create(String ownerId,
              String title,
              int startingBid,
              Result<String> result);

  void open(Result<Boolean> result);

  void close(Result<Boolean> result);

  void getAuctionData(Result<AuctionDataPublic> result);

  void bid(String userId, int bid, Result<Boolean> result)
    throws IllegalStateException;
}
