package examples.auction.s1;

import examples.auction.Payment;
import examples.auction.Refund;

public class TransactionState
{
  private CommitState _commitState;
  private RollbackState _rollbackState;
  private Payment _payment;
  private Refund _refund;

  public TransactionState()
  {
    _commitState = CommitState.PENDING;
  }

  public CommitState getCommitState()
  {
    return _commitState;
  }

  public void setCommitState(CommitState commitState)
  {
    _commitState = commitState;
  }

  public RollbackState getRollbackState()
  {
    return _rollbackState;
  }

  public void setRollbackState(RollbackState rollbackState)
  {
    _rollbackState = rollbackState;
  }

  public Payment getPayment()
  {
    return _payment;
  }

  public void setPayment(Payment payment)
  {
    _payment = payment;
  }

  public void setRefund(Refund refund)
  {
    _refund = refund;
  }

  public Refund getRefund()
  {
    return _refund;
  }

  @Override
  public String toString()
  {
    return this.getClass().getSimpleName()
           + "["
           + _commitState
           + ", "
           + _rollbackState
           + "]";
  }

  enum CommitState
  {
    COMPLETED,
    PENDING,
    REJECTED_PAYMENT,
    REJECTED_USER,
    REJECTED_AUCTION
  }

  enum RollbackState
  {
    COMPLETED,
    PENDING,
    REFUND_FAILED
  }
}
