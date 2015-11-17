package examples.auction.s1;

public class TransactionState
{
  private CommitState _commitState;
  private RollbackState _rollbackState;

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
