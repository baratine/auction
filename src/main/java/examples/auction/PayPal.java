package examples.auction;

import io.baratine.core.Result;

import java.io.IOException;

public interface PayPal
{
  public void settle(AuctionDataPublic auction,
                     CreditCard creditCard,
                     String userId,
                     String idempotencyKey,
                     Result<Payment> result) throws IOException;
}
