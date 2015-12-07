package examples.auction.mock;

import examples.auction.AuctionDataPublic;
import examples.auction.CreditCard;
import examples.auction.PayPal;
import examples.auction.Payment;
import examples.auction.Refund;
import io.baratine.core.Result;
import io.baratine.core.Service;

@Service("pod://auction/paypal")
public class MockPayPal implements PayPal
{
  private Payment _payment;

  @Override
  public void settle(AuctionDataPublic auction,
                     AuctionDataPublic.Bid bid,
                     CreditCard creditCard,
                     String userId,
                     String payPalRequestId,
                     Result<Payment> result)
  {
    result.complete(_payment);
  }

  @Override
  public void refund(String settlementId,
                     String payPalRequestId,
                     String sale,
                     Result<Refund> result)
  {
    Refund refund = new MockRefund(Refund.RefundState.completed);

    result.complete(refund);
  }

  public void setPaymentResult(Payment payment, Result<Void> result)
  {
    _payment = payment;

    result.complete(null);
  }
}
