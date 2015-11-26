package examples.auction;

public interface AuctionSync extends Auction
{
  String create(AuctionDataInit initData);

  boolean open();

  boolean close();

  AuctionDataPublic get();

  boolean bid(Bid bid);

  String getSettlementId();
}
