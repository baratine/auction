package examples.auction;

import io.baratine.service.Result;

public interface PayPal
{
  void settle(AuctionDataPublic auction,
              AuctionDataPublic.Bid bid,
              CreditCard creditCard,
              String payPalRequestId,
              Result<Payment> result);

  void refund(String settlementId,
              String payPalRequestId,
              String sale,
              Result<Refund> result);
}
