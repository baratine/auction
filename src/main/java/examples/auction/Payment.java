package examples.auction;

import javax.json.JsonObject;
import java.io.StringReader;

/**
 * Class payment encapsulates PayPal reply
 */
public class Payment
{
  private String _payment;

  private PayPalResult _status;

  public Payment()
  {
  }

  public Payment(String payment)
  {
    _payment = payment;

    JsonObject jsonObject
      = javax.json.Json.createReader(new StringReader(payment)).readObject();

    String state = jsonObject.getString("state");

    _status = Enum.valueOf(PayPalResult.class, state);
  }

  public PayPalResult getStatus()
  {
    return _status;
  }

  @Override
  public String toString()
  {
    return Payment.class.getSimpleName() + "[" + _payment + "]";
  }

  public enum PayPalResult
  {
    created, approved, failed, canceled, expired, pending;
  }
}
