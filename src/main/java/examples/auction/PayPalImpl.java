package examples.auction;

import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.Workers;

@Service("pod://auction/paypal")
@Workers(20)
public class PayPalImpl implements PayPal
{
  @Override
  public void settle(AuctionDataPublic auction,
                     CreditCard creditCard,
                     String userId,
                     String idempotencyKey,
                     Result<Payment> result)
  {

  }
}

