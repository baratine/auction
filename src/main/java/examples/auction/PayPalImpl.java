package examples.auction;

import io.baratine.core.Lookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.Workers;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service("pod://auction/paypal")
@Workers(20)
public class PayPalImpl implements PayPal
{
  private static final Logger log
    = Logger.getLogger(PayPalImpl.class.getName());

  @Inject
  PayPalRestLink _rest;

  @Inject
  @Lookup("pod://auction/settlement")
  AuctionSettlement _settlement;

  @Override
  public void settle(AuctionDataPublic auction,
                     CreditCard creditCard,
                     String userId,
                     String idempotencyKey,
                     Result<Payment> result)
  {
    try {
      log.log(Level.FINER, String.format("settle payment for auction %1$s",
                                         auction));

      PayPalAuth auth = _rest.auth();

      String amount = String.format("%1$d.00", auction.getLastBid().getBid());

      Payment payment = _rest.pay(auth.getToken(),
                                  idempotencyKey,
                                  creditCard.getNum(),
                                  creditCard.getType(),
                                  creditCard.getExpMonth(),
                                  creditCard.getExpYear(),
                                  creditCard.getCvv(),
                                  "John",
                                  "Doe",
                                  amount,
                                  "USD",
                                  auction.getTitle());

      log.log(Level.FINER, String.format(
        "payment recieved for auction %1$s -> %2$s ",
        auction.getId(),
        payment));

      result.complete(payment);
    } catch (Throwable t) {
      log.log(Level.WARNING, t.getMessage(), t);

      result.fail(t);
    }
  }
}
