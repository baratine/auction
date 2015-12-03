package examples.auction;

public interface Payment
{
  PaymentState getState();

  String getSaleId();

  public enum PaymentState
  {
    created, approved, failed, canceled, expired, pending;
  }
}
