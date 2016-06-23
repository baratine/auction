package examples.auction.mock;

import io.baratine.service.Result;
import io.baratine.service.Service;

import examples.auction.Auction;
import examples.auction.AuctionData;
import examples.auction.CreditCard;
import examples.auction.PayPal;
import examples.auction.Payment;
import examples.auction.Refund;

@Service("/PayPal")
public class MockPayPal implements PayPal
{
  public static boolean isSettle = true;

  private Payment _payment = new MockPayment("sale-id-0",
                                             Payment.PaymentState.approved);
  private long _sleep = 0;

  @Override
  public void settle(AuctionData auction,
                     Auction.Bid bid,
                     CreditCard creditCard,
                     String payPalRequestId,
                     Result<Payment> result)
  {
    if (isSettle) {
      try {
        Thread.sleep(_sleep);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      result.ok(_payment);
    }
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

  public void configure(Payment payment,
                        long sleep,
                        Result<Void> result)
  {
    _payment = payment;
    _sleep = sleep;

    result.ok(null);
  }
}
