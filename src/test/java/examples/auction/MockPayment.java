package examples.auction;

public class MockPayment implements Payment
{
  private String _saleId;
  private PaymentState _state;

  public MockPayment()
  {
  }

  public MockPayment(String saleId, PaymentState state)
  {
    _saleId = saleId;
    _state = state;
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
}
