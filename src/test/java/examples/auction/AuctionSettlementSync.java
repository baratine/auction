package examples.auction;

public interface AuctionSettlementSync extends AuctionSettlement
{
  Status settle(Auction.Bid bid);

  Status refund();

  Status settleStatus();

  Status refundStatus();

  SettlementTransactionState getTransactionState();
}
