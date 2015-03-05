package examples.auction;

public interface SyncAuctionManager extends AuctionManager
{
  String create(String ownerId, String title, int bid);

  String find(String title);
}
