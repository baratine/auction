package examples.auction;

import io.baratine.core.Result;

public interface Auction
{
  void create(AuctionDataInit initData,
              Result<String> result);

  void open(Result<Boolean> result);

  void bid(Bid bid, Result<Boolean> result)
    throws IllegalStateException;

  void setPendingAuctionWinner(String user, Result<Boolean> result);

  void clearAuctionWinner(String user, Result<Boolean> result);

  void get(Result<AuctionDataPublic> result);

  void close(Result<Boolean> result);
}

