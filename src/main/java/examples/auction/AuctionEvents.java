package examples.auction;

public interface AuctionEvents
{
  void onBid(AuctionData auctionData);

  void onClose(AuctionData auctionData);

  void onSettled(AuctionData auctionData);

  void onRolledBack(AuctionData auctionData);
}
