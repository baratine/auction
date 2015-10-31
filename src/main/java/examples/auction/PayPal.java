package examples.auction;

import io.baratine.core.Result;

public interface PayPal
{
  void settle(AuctionDataPublic auction,
              CreditCard creditCard,
              String userId,
              String idempotencyKey,
              Result<Payment> result);
}
