package examples.auction;

public interface PayPalSync extends PayPal
{
  Payment settle(AuctionDataPublic auction,
                 AuctionDataPublic.Bid bid,
                 CreditCard creditCard,
                 String userId,
                 String payPalRequestId);

  Refund refund(String settlementId,
                String payPalRequestId,
                String sale);

  void setPayToSucceed(boolean succeed);
}
