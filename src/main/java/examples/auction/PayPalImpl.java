package examples.auction;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Workers;

@Service("/paypal")
@Workers(20)
public class PayPalImpl implements PayPal
{
  private static final Logger log
    = Logger.getLogger(PayPalImpl.class.getName());

  @Inject
  PayPalRestLink _rest;

  @Inject
  @Service("/audit")
  AuditService _audit;

  @Override
  public void settle(AuctionData auction,
                     Auction.Bid bid,
                     CreditCard creditCard,
                     String payPalRequestId,
                     Result<Payment> result)
  {
    try {
      log.log(Level.FINER, String.format("settle payment for auction %1$s",
                                         auction));

      PayPalAuth auth = _rest.auth();

      String amount = String.format("%1$d.00", bid.getBid());

      _audit.payPalSendPaymentRequest(payPalRequestId,
                                      auction,
                                      bid,
                                      bid.getUserId(),
                                      Result.ignore());

      Payment payment = _rest.pay(auth.getToken(),
                                  payPalRequestId,
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

      _audit.payPalReceivePaymentResponse(payPalRequestId,
                                          auction,
                                          payment,
                                          Result.ignore());

      log.log(Level.FINER, String.format(
        "payment recieved for auction %1$s -> %2$s ",
        auction.getEncodedId(),
        payment));

      result.ok(payment);
    } catch (Throwable t) {
      log.log(Level.WARNING, t.getMessage(), t);

      result.fail(t);
    }
  }

  @Override
  public void refund(String settlementId,
                     String payPalRequestId,
                     String salesId,
                     Result<Refund> result)
  {
    try {
      _audit.payPalSendRefund(settlementId, salesId, Result.ignore());
      PayPalAuth auth = _rest.auth();
      Refund refund = _rest.refund(auth.getToken(), payPalRequestId, salesId);

      result.ok(refund);

      _audit.payPalReceiveRefundResponse(settlementId,
                                         salesId,
                                         refund,
                                         Result.ignore());
    } catch (Throwable e) {
      result.fail(e);
    }
  }
}
