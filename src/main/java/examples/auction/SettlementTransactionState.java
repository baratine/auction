package examples.auction;

public class SettlementTransactionState
{
  private Intent _intent = Intent.COMMIT;

  private AuctionSettlement.Status _commitStatus
    = AuctionSettlement.Status.COMMITTING;

  private AuctionSettlement.Status _rollbackStatus
    = AuctionSettlement.Status.NONE;

  private UserUpdateState _userCommitState = UserUpdateState.NONE;
  private AuctionWinnerUpdateState
    _auctionWinnerUpdateState = AuctionWinnerUpdateState.NONE;
  private PaymentTxState _paymentCommitState = PaymentTxState.NONE;
  private AuctionUpdateState _auctionStateUpdateState = AuctionUpdateState.NONE;

  private UserUpdateState _userRollbackState = UserUpdateState.NONE;
  private AuctionWinnerUpdateState
    _auctionWinnerRollbackState = AuctionWinnerUpdateState.NONE;
  private PaymentTxState _paymentRollbackState = PaymentTxState.NONE;

  private Payment _payment;
  private Refund _refund;

  public SettlementTransactionState()
  {
  }

  public void toRollBack()
  {
    _intent = Intent.ROLLBACK;
  }

  public UserUpdateState getUserCommitState()
  {
    return _userCommitState;
  }

  public void setUserCommitState(UserUpdateState userCommitState)
  {
    _userCommitState = userCommitState;
  }

  public AuctionWinnerUpdateState getAuctionWinnerUpdateState()
  {
    return _auctionWinnerUpdateState;
  }

  public void setAuctionWinnerUpdateState(AuctionWinnerUpdateState auctionWinnerUpdateState)
  {
    _auctionWinnerUpdateState = auctionWinnerUpdateState;
  }

  public PaymentTxState getPaymentCommitState()
  {
    return _paymentCommitState;
  }

  public void setPaymentCommitState(PaymentTxState paymentCommitState)
  {
    _paymentCommitState = paymentCommitState;
  }

  public UserUpdateState getUserRollbackState()
  {
    return _userRollbackState;
  }

  public void setUserRollbackState(UserUpdateState userRollbackState)
  {
    _userRollbackState = userRollbackState;
  }

  public AuctionWinnerUpdateState getAuctionWinnerRollbackState()
  {
    return _auctionWinnerRollbackState;
  }

  public void setAuctionWinnerRollbackState(AuctionWinnerUpdateState auctionWinnerRollbackState)
  {
    _auctionWinnerRollbackState = auctionWinnerRollbackState;
  }

  public PaymentTxState getPaymentRollbackState()
  {
    return _paymentRollbackState;
  }

  public void setPaymentRollbackState(PaymentTxState paymentRollbackState)
  {
    _paymentRollbackState = paymentRollbackState;
  }

  public AuctionUpdateState getAuctionStateUpdateState()
  {
    return _auctionStateUpdateState;
  }

  public void setAuctionStateUpdateState(AuctionUpdateState auctionStateUpdateState)
  {
    _auctionStateUpdateState = auctionStateUpdateState;
  }

  public boolean isCommitted()
  {
    boolean isCommitted = _intent == Intent.COMMIT;

    isCommitted &= _userCommitState == UserUpdateState.SUCCESS;

    isCommitted &= _auctionWinnerUpdateState
                   == AuctionWinnerUpdateState.SUCCESS;

    isCommitted &= _paymentCommitState == PaymentTxState.SUCCESS;

    return isCommitted;
  }

  public boolean isCommitting()
  {
    return _intent == Intent.COMMIT;
  }

  public boolean isRolledBack()
  {
    boolean isRolledBack = _intent == Intent.ROLLBACK;

    isRolledBack &= (_userCommitState == UserUpdateState.REJECTED
                     || _userCommitState == UserUpdateState.ROLLED_BACK);

    isRolledBack &= (_auctionWinnerUpdateState
                     == AuctionWinnerUpdateState.REJECTED
                     || _auctionWinnerUpdateState
                        == AuctionWinnerUpdateState.ROLLED_BACK);

    isRolledBack &= (_paymentCommitState == PaymentTxState.REFUNDED
                     || _paymentCommitState == PaymentTxState.FAILED);

    return isRolledBack;
  }

  public boolean isRollingBack()
  {
    return _intent == Intent.ROLLBACK;
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

  public AuctionSettlement.Status getRollbackStatus()
  {
    return _rollbackStatus;
  }

  public void setRollbackStatus(AuctionSettlement.Status rollbackStatus)
  {
    _rollbackStatus = rollbackStatus;
  }

  @Override
  public String toString()
  {
    return this.getClass().getSimpleName()
           + "["
           + _intent
           + ", "
           + _userCommitState + ", "
           + _auctionWinnerUpdateState + ", "
           + _paymentCommitState
           + "]";
  }

  enum Intent
  {
    COMMIT,
    ROLLBACK
  }

  enum UserUpdateState
  {
    SUCCESS,
    REJECTED,
    ROLLED_BACK,
    NONE
  }

  enum AuctionWinnerUpdateState
  {
    SUCCESS,
    REJECTED,
    ROLLED_BACK,
    NONE
  }

  enum AuctionUpdateState
  {
    SUCCESS,
    ROLLED_BACK,
    NONE
  }

  enum PaymentTxState
  {
    SUCCESS,
    PENDING,
    FAILED,
    REFUNDED,
    NONE
  }
}
