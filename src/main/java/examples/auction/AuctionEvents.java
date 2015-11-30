package examples.auction;

public interface AuctionEvents
{
  void onBid(AuctionDataPublic auctionData);

  void onClose(AuctionDataPublic auctionData);

  void onSettled(AuctionDataPublic auctionData);

  void onRolledBack(AuctionDataPublic auctionData);
}
