package examples.auction;

public interface Refund
{
  RefundState getStatus();

  public enum RefundState
  {
    pending, completed, failed
  }
}
