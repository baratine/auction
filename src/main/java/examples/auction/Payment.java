package examples.auction;

public interface Payment
{
  PaymentState getState();

  String getSaleId();

  enum PaymentState
  {
    created, approved, failed, canceled, expired, pending;
  }
}
