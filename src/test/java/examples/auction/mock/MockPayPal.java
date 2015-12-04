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
  private boolean _isPaymentToSucceed = true;

  @Override
  public void settle(AuctionDataPublic auction,
                     AuctionDataPublic.Bid bid,
                     CreditCard creditCard,
                     String userId,
                     String payPalRequestId,
                     Result<Payment> result)
  {
    Payment payment;
    if (_isPaymentToSucceed) {
      payment = new MockPayment("sale-id", Payment.PaymentState.approved);

    }
    else {
      payment = new MockPayment("sale-id", Payment.PaymentState.failed);
    }

    result.complete(payment);
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

  public void setPayToSucceed(boolean succeed, Result<Void> result)
  {
    _isPaymentToSucceed = succeed;

    result.complete(null);
  }
}
