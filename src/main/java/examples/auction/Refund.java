package examples.auction;

import javax.json.JsonObject;
import java.io.StringReader;

/**
 * {
 * "id": "9JU80416NM254722B",
 * "create_time": "2015-11-20T17:33:02Z",
 * "update_time": "2015-11-20T17:33:02Z",
 * "state": "completed",
 * "amount": {
 * "total": "6.00",
 * "currency": "USD"
 * },
 * "sale_id": "6W4936389X1091947",
 * "parent_payment": "PAY-2X7976490R398621MKZB6KSQ",
 * "links": [
 * {
 * "href": "https://api.sandbox.paypal.com/v1/payments/refund/9JU80416NM254722B",
 * "rel": "self",
 * "method": "GET"
 * },
 * {
 * "href": "https://api.sandbox.paypal.com/v1/payments/payment/PAY-2X7976490R398621MKZB6KSQ",
 * "rel": "parent_payment",
 * "method": "GET"
 * },
 * {
 * "href": "https://api.sandbox.paypal.com/v1/payments/sale/6W4936389X1091947",
 * "rel": "sale",
 * "method": "GET"
 * }
 * ]
 * }
 */
public class Refund
{
  private String _refund;
  private RefundState _status;

  public Refund(String refund)
  {
    _refund = refund;

    JsonObject jsonObject
      = javax.json.Json.createReader(new StringReader(refund)).readObject();

    String state = jsonObject.getString("state");

    _status = Enum.valueOf(RefundState.class, state);
  }

  public RefundState getStatus()
  {
    return _status;
  }

  public enum RefundState
  {
    pending, completed, failed
  }
}
