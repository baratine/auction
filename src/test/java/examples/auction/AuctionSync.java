package examples.auction;

public interface AuctionSync extends Auction
{
  String create(AuctionDataInit initData);

  boolean open();

  boolean close();

  AuctionDataPublic get();

  boolean bid(String userId, int bid);
}
