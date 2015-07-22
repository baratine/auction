package examples.auction;

public interface AuctionSync extends Auction
{
  String create(String userId, String title, int startingBid);

  boolean open();

  boolean close();

  AuctionDataPublic get();

  boolean bid(String userId, int bid);
}
