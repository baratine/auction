package examples.auction;

import io.baratine.service.Api;
import io.baratine.service.Modify;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.vault.Asset;
import io.baratine.vault.IdAsset;

@Asset
@Api
public interface Auction
{
  @Modify
  void create(AuctionDataInit initData,
              Result<IdAsset> auctionId);

  @Modify
  void open(Result<Boolean> result);

  @Modify
  void bid(AuctionBid bid, Result<Boolean> result)
    throws IllegalStateException;

  @Modify
  void setAuctionWinner(String user, Result<Boolean> result);

  @Modify
  void clearAuctionWinner(String user, Result<Boolean> result);

  @Modify
  void setSettled(Result<Boolean> result);

  @Modify
  void setRolledBack(Result<Boolean> result);

  void get(Result<AuctionData> result);

  @Modify
  void close(Result<Boolean> result);

  @Modify
  void refund(Result<Boolean> result);

  void getSettlementId(Result<String> result);

  enum State
  {
    INIT,
    OPEN,
    CLOSED,
    SETTLED,
    ROLLED_BACK
  }

  interface Bid
  {
    String getAuctionId();

    String getUserId();

    int getBid();
  }
}

