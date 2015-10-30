package examples.auction;

/**
 * Class payment encapsulates PayPal reply
 */
public class Payment
{
  private String _payment;

  private PayPalResult _status;

  public Payment(String payment, PayPalResult status)
  {
    _payment = payment;
  }

  public PayPalResult getStatus()
  {
    return _status;
  }

  public enum PayPalResult
  {
    created, approved, failed, canceled, expired, pending;
  }
}
