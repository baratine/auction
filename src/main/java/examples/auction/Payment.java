package examples.auction;

import javax.json.JsonObject;
import java.io.StringReader;

/**
 * Class payment encapsulates PayPal reply
 */
public class Payment
{
  private String _payment;

  private PaymentState _state;
  private String _saleId;

  public Payment()
  {
  }

  public Payment(String payment)
  {
    _payment = payment;

    JsonObject jsonObject
      = javax.json.Json.createReader(new StringReader(payment)).readObject();

    String state = jsonObject.getString("state");

    _state = Enum.valueOf(PaymentState.class, state);

    _saleId = jsonObject.getJsonArray("transactions").getJsonObject(0)
                        .getJsonArray("related_resources").getJsonObject(0)
                        .getJsonObject("sale").getString("id");
  }

  public PaymentState getState()
  {
    return _state;
  }

  public String getSaleId()
  {
    return _saleId;
  }

  @Override
  public String toString()
  {
    return Payment.class.getSimpleName() + "[" + _payment + "]";
  }

  public enum PaymentState
  {
    created, approved, failed, canceled, expired, pending;
  }
}
