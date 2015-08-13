package examples.auction;

public interface AuctionManagerSync extends AuctionManager
{
  String create(AuctionDataInit initData);

  String find(String title);
}
