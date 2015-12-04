package examples.auction;

public interface AuctionSettlementSync extends AuctionSettlement
{

  boolean create(String auctionId, String userId, AuctionDataPublic.Bid bid);

  Status commit();

  Status rollback();

  Status status();

  SettlementTransactionState getTransactionState();
}
