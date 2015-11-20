package examples.auction.s1;

import examples.auction.Payment;

public class TransactionState
{
  private CommitState _commitState;
  private RollbackState _rollbackState;
  private Payment _payment;

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
    FAILED
  }
}
