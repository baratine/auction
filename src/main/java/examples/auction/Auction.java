package examples.auction;

import io.baratine.service.IdAsset;
import io.baratine.service.Result;

public interface Auction
{
  void create(AuctionDataInit initData,
              Result<IdAsset> auctionId);

  void open(Result<Boolean> result);

  void bid(AuctionBid bid, Result<Boolean> result)
    throws IllegalStateException;

  void setAuctionWinner(String user, Result<Boolean> result);

  void clearAuctionWinner(String user, Result<Boolean> result);

  void setSettled(Result<Boolean> result);

  void setRolledBack(Result<Boolean> result);

  void get(Result<AuctionData> result);

  void close(Result<Boolean> result);

  void refund(Result<Boolean> result);

  void getSettlementId(Result<String> result);

  interface Bid
  {
    String getAuctionId();

    String getUserId();

    int getBid();
  }

  enum State
  {
    INIT,
    OPEN,
    CLOSED,
    SETTLED,
    ROLLED_BACK
  }
}

