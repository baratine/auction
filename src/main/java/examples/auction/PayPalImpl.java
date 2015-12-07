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
  @Lookup("pod://audit/audit")
  AuditService _audit;

  @Override
  public void settle(AuctionDataPublic auction,
                     AuctionDataPublic.Bid bid,
                     CreditCard creditCard,
                     String userId,
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
                                      userId,
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
        auction.getId(),
        payment));

      result.complete(payment);
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

      result.complete(refund);

      _audit.payPalReceiveRefundResponse(settlementId,
                                         salesId,
                                         refund,
                                         Result.ignore());
    } catch (Throwable e) {
      result.fail(e);
    }
  }
}
