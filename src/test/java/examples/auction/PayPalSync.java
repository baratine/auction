package examples.auction;

public interface PayPalSync extends PayPal
{
  Payment settle(AuctionData auction,
                 Auction.Bid bid,
                 CreditCard creditCard,
                 String payPalRequestId);

  Refund refund(String settlementId,
                String payPalRequestId,
                String sale);

  void configure(Payment state, long sleep);
}
