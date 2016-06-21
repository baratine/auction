package examples.auction;

import io.baratine.vault.IdAsset;

public interface AuctionSync extends Auction
{
  IdAsset create(AuctionDataInit initData);

  boolean open();

  boolean bid(AuctionBid bid)
    throws IllegalStateException;

  boolean setAuctionWinner(String user);

  boolean clearAuctionWinner(String user);

  boolean setSettled();

  boolean setRolledBack();

  AuctionData get();

  boolean close();

  boolean refund();

  String getSettlementId();
}
