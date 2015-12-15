package examples.auction;

public class SettlementTransactionState
{
  private Intent _intent = Intent.COMMIT;

  private AuctionSettlement.Status _settleStatus
    = AuctionSettlement.Status.SETTLING;

  private AuctionSettlement.Status _refundStatus
    = AuctionSettlement.Status.NONE;

  private UserUpdateState _userSettleState = UserUpdateState.NONE;

  private AuctionWinnerUpdateState
    _auctionWinnerUpdateState = AuctionWinnerUpdateState.NONE;

  private PaymentTxState _paymentState = PaymentTxState.NONE;
  private AuctionUpdateState _auctionStateUpdateState = AuctionUpdateState.NONE;

  private UserUpdateState _userResetState = UserUpdateState.NONE;
  private AuctionWinnerUpdateState
    _auctionWinnerResetState = AuctionWinnerUpdateState.NONE;
  private PaymentTxState _refundState = PaymentTxState.NONE;

  private Payment _payment;
  private Refund _refund;

  public SettlementTransactionState()
  {
  }

  public void toRefund()
  {
    _intent = Intent.REFUND;
  }

  public UserUpdateState getUserSettleState()
  {
    return _userSettleState;
  }

  public void setUserSettleState(UserUpdateState userSettleState)
  {
    _userSettleState = userSettleState;
  }

  public AuctionWinnerUpdateState getAuctionWinnerUpdateState()
  {
    return _auctionWinnerUpdateState;
  }

  public void setAuctionWinnerUpdateState(AuctionWinnerUpdateState auctionWinnerUpdateState)
  {
    _auctionWinnerUpdateState = auctionWinnerUpdateState;
  }

  public PaymentTxState getPaymentState()
  {
    return _paymentState;
  }

  public void setPaymentState(PaymentTxState paymentState)
  {
    _paymentState = paymentState;
  }

  public UserUpdateState getUserResetState()
  {
    return _userResetState;
  }

  public void setUserResetState(UserUpdateState userResetState)
  {
    _userResetState = userResetState;
  }

  public AuctionWinnerUpdateState getAuctionWinnerResetState()
  {
    return _auctionWinnerResetState;
  }

  public void setAuctionWinnerResetState(AuctionWinnerUpdateState auctionWinnerResetState)
  {
    _auctionWinnerResetState = auctionWinnerResetState;
  }

  public PaymentTxState getRefundState()
  {
    return _refundState;
  }

  public void setRefundState(PaymentTxState refundState)
  {
    _refundState = refundState;
  }

  public AuctionUpdateState getAuctionStateUpdateState()
  {
    return _auctionStateUpdateState;
  }

  public void setAuctionStateUpdateState(AuctionUpdateState auctionStateUpdateState)
  {
    _auctionStateUpdateState = auctionStateUpdateState;
  }

  public boolean isSettled()
  {
    boolean isCommitted = _intent == Intent.COMMIT;

    isCommitted &= _userSettleState == UserUpdateState.SUCCESS;

    isCommitted &= _auctionWinnerUpdateState
                   == AuctionWinnerUpdateState.SUCCESS;

    isCommitted &= _paymentState == PaymentTxState.SUCCESS;

    return isCommitted;
  }

  public boolean isCommitting()
  {
    return _intent == Intent.COMMIT;
  }

  public boolean isRefunded()
  {
    boolean isRefunded = _intent == Intent.REFUND;

    isRefunded &= (_userSettleState == UserUpdateState.REJECTED
                   || _userResetState == UserUpdateState.ROLLED_BACK);

    isRefunded &= (_auctionWinnerUpdateState
                   == AuctionWinnerUpdateState.REJECTED
                   || _auctionWinnerResetState
                      == AuctionWinnerUpdateState.ROLLED_BACK);

    isRefunded &= (_paymentState == PaymentTxState.FAILED
                   || _refundState == PaymentTxState.REFUNDED);

    return isRefunded;
  }

  public boolean isRefunding()
  {
    return _intent == Intent.REFUND;
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

  public void setSettleStatus(AuctionSettlement.Status commitStatus)
  {
    _settleStatus = commitStatus;
  }

  public AuctionSettlement.Status getSettleStatus()
  {
    return _settleStatus;
  }

  public AuctionSettlement.Status getRefundStatus()
  {
    return _refundStatus;
  }

  public void setRefundStatus(AuctionSettlement.Status refundStatus)
  {
    _refundStatus = refundStatus;
  }

  @Override
  public String toString()
  {
    return this.getClass().getSimpleName()
           + "["
           + _intent
           + ", "
           + _userSettleState + ", "
           + _auctionWinnerUpdateState + ", "
           + _paymentState
           + "]";
  }

  enum Intent
  {
    COMMIT,
    REFUND
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
