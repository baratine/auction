package examples.auction;

import io.baratine.service.Result;

public interface Auction
{
  void create(AuctionDataInit initData,
              Result<Long> result);

  void open(Result<Boolean> result);

  void bid(Bid bid, Result<Boolean> result)
    throws IllegalStateException;

  void setAuctionWinner(long user, Result<Boolean> result);

  void clearAuctionWinner(long user, Result<Boolean> result);

  void setSettled(Result<Boolean> result);

  void setRolledBack(Result<Boolean> result);

  void get(Result<AuctionDataPublic> result);

  void close(Result<Boolean> result);

  void refund(Result<Boolean> result);

  void getSettlementId(Result<String> result);
}

