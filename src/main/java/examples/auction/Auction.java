package examples.auction;

import io.baratine.core.Result;

public interface Auction
{
  void create(AuctionDataInit initData,
              Result<String> result);

  void open(Result<Boolean> result);

  void bid(Bid bid, Result<Boolean> result)
    throws IllegalStateException;

  void setAuctionWinner(String user, Result<Boolean> result);

  void resetAuctionWinner(String user, Result<Boolean> result);

  void get(Result<AuctionDataPublic> result);

  void close(Result<Boolean> result);
}

