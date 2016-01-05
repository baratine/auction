package examples.auction.mock;

import examples.auction.AuctionDataPublic;
import examples.auction.CreditCard;
import examples.auction.PayPal;
import examples.auction.Payment;
import examples.auction.Refund;
import io.baratine.service.Result;
import io.baratine.service.Service;

@Service("public:///paypal")
public class MockPayPal implements PayPal
{
  private Payment _payment;

  @Override
  public void settle(AuctionDataPublic auction,
                     AuctionDataPublic.Bid bid,
                     CreditCard creditCard,
                     String payPalRequestId,
                     Result<Payment> result)
  {
    result.ok(_payment);
  }

  @Override
  public void refund(String settlementId,
                     String payPalRequestId,
                     String sale,
                     Result<Refund> result)
  {
    Refund refund = new MockRefund(Refund.RefundState.completed);

    result.ok(refund);
  }

  public void setPaymentResult(Payment payment, Result<Void> result)
  {
    _payment = payment;

    result.ok(null);
  }
}
