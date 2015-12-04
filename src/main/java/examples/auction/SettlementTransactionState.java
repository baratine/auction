package examples.auction;

public class SettlementTransactionState
{
  private CommitPhase _commitPhase = CommitPhase.COMMITTING;

  private AuctionSettlement.Status _commitStatus
    = AuctionSettlement.Status.COMMITTING;

  private UserUpdateState _userCommitState = UserUpdateState.UNKNOWN;
  private AuctionUpdateState _auctionCommitState = AuctionUpdateState.UNKNOWN;
  private PaymentTxState _paymentCommitState = PaymentTxState.UNKNOWN;

  private Payment _payment;
  private Refund _refund;

  public SettlementTransactionState()
  {
  }

  public void toRollBack()
  {
    _commitPhase = CommitPhase.ROLLING_BACK;
  }

  public UserUpdateState getUserCommitState()
  {
    return _userCommitState;
  }

  public void setUserCommitState(UserUpdateState userCommitState)
  {
    _userCommitState = userCommitState;
  }

  public AuctionUpdateState getAuctionCommitState()
  {
    return _auctionCommitState;
  }

  public void setAuctionCommitState(AuctionUpdateState auctionCommitState)
  {
    _auctionCommitState = auctionCommitState;
  }

  public PaymentTxState getPaymentCommitState()
  {
    return _paymentCommitState;
  }

  public void setPaymentCommitState(PaymentTxState paymentCommitState)
  {
    _paymentCommitState = paymentCommitState;
  }

  public boolean isCommitted()
  {
    boolean isCommitted = _commitPhase == CommitPhase.COMMITTING;

    isCommitted &= _userCommitState == UserUpdateState.SUCCESS;

    isCommitted &= _auctionCommitState == AuctionUpdateState.SUCCESS;

    isCommitted &= _paymentCommitState == PaymentTxState.SUCCESS;

    return isCommitted;
  }

  public boolean isCommitting()
  {
    return _commitPhase == CommitPhase.COMMITTING;
  }

  public boolean isRolledBack()
  {
    boolean isRolledBack = _commitPhase == CommitPhase.ROLLING_BACK;

    isRolledBack &= (_userCommitState == UserUpdateState.REJECTED
                     || _userCommitState == UserUpdateState.ROLLED_BACK);

    isRolledBack &= (_auctionCommitState == AuctionUpdateState.REJECTED
                     || _auctionCommitState == AuctionUpdateState.ROLLED_BACK);

    isRolledBack &= (_paymentCommitState == PaymentTxState.REFUNDED
                     || _paymentCommitState == PaymentTxState.FAILED);

    return isRolledBack;

  }

  public boolean isRollingBack()
  {
    return _commitPhase == CommitPhase.ROLLING_BACK;
  }

  public Payment getPayment()
  {
    return _payment;
  }

  public void setPayment(Payment payment)
  {
    _payment = payment;
  }

  public Refund getRefund()
  {
    return _refund;
  }

  public void setRefund(Refund refund)
  {
    _refund = refund;
  }

  public void setCommitStatus(AuctionSettlement.Status commitStatus)
  {
    _commitStatus = commitStatus;
  }

  public AuctionSettlement.Status getCommitStatus()
  {
    return _commitStatus;
  }

  @Override
  public String toString()
  {
    return this.getClass().getSimpleName()
           + "["
           + _commitPhase
           + ", "
           + _userCommitState + ", "
           + _auctionCommitState + ", "
           + _paymentCommitState
           + "]";
  }

  enum CommitPhase
  {
    COMMITTING,
    ROLLING_BACK
  }

  enum UserUpdateState
  {
    SUCCESS,
    REJECTED,
    ROLLED_BACK,
    UNKNOWN
  }

  enum AuctionUpdateState
  {
    SUCCESS,
    REJECTED,
    ROLLED_BACK,
    UNKNOWN
  }

  enum PaymentTxState
  {
    SUCCESS,
    FAILED,
    REFUNDED,
    UNKNOWN
  }
}
