package examples.auction;

public interface SyncAuction extends Auction
{
  String create(String ownerId, String title, int startingBid);

  boolean open();

  boolean close();

  AuctionDataPublic getAuctionData();

  boolean bid(String userId, int bid);
}
