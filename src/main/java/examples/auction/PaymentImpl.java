package examples.auction;

import java.io.StringReader;

import javax.json.JsonObject;

/**
 * Class payment encapsulates PayPal reply
 */
public class PaymentImpl implements Payment
{
  private String _payment;

  private PaymentState _state;
  private String _saleId;

  public PaymentImpl()
  {
  }

  public PaymentImpl(String payment)
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

  @Override
  public PaymentState getState()
  {
    return _state;
  }

  @Override
  public String getSaleId()
  {
    return _saleId;
  }

  @Override
  public String toString()
  {
    return PaymentImpl.class.getSimpleName() + "[" + _payment + "]";
  }

}
