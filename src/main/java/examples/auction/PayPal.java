package examples.auction;

import io.baratine.core.Result;

public interface PayPal
{
  void settle(AuctionDataPublic auction,
              AuctionDataPublic.Bid bid,
              CreditCard creditCard,
              String userId,
              String payPalRequestId,
              Result<Payment> result);

  void refund(String settlementId,
              String payPalRequestId,
              String sale,
              Result<Refund> refund);
}
