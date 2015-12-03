package examples.auction;

import io.baratine.core.Result;
import io.baratine.core.Service;

@Service("pod://auction/paypal")
public class MockPayPal implements PayPal
{
  @Override
  public void settle(AuctionDataPublic auction,
                     AuctionDataPublic.Bid bid,
                     CreditCard creditCard,
                     String userId,
                     String payPalRequestId,
                     Result<Payment> result)
  {
    Payment payment = new PaymentImpl();

  }

  @Override
  public void refund(String settlementId,
                     String payPalRequestId,
                     String sale,
                     Result<Refund> refund)
  {

  }
}
