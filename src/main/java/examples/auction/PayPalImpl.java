package examples.auction;

import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.Workers;

import javax.inject.Inject;
import java.io.IOException;

@Service("pod://auction/paypal")
@Workers(20)
public class PayPalImpl implements PayPal
{
  @Inject
  PayPalRestLink _rest;

  @Override
  public void settle(AuctionDataPublic auction,
                     CreditCard creditCard,
                     String userId,
                     String idempotencyKey,
                     Result<Payment> result) throws IOException
  {
    PayPalAuth auth = _rest.auth();

    Payment pay = _rest.pay(auth.getToken(),
                            idempotencyKey,
                            creditCard.getNum(),
                            creditCard.getType(),
                            creditCard.getExpMonth(),
                            creditCard.getExpYear(),
                            creditCard.getCvv(),
                            "first-name",
                            "last-name",
                            Integer.toString(auction.getLastBid().getBid()),
                            "USD",
                            auction.getTitle());

  }
}

