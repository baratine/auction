package examples.auction.s1;

import examples.auction.Payment;
import examples.auction.Refund;

public class TransactionState
{
  private CommitPhase _commitPhase = CommitPhase.COMMITTING;

  private UserUpdateState _userUpdateState = UserUpdateState.UNKNOWN;
  private AuctionUpdateState _auctionUpdateState = AuctionUpdateState.UNKNOWN;
  private PaymentState _paymentState = PaymentState.UNKNOWN;

  private Payment _payment;
  private Refund _refund;

  public TransactionState()
  {
  }

  public void toRollBack()
  {
    _commitPhase = CommitPhase.ROLLING_BACK;
  }

  public UserUpdateState getUserUpdateState()
  {
    return _userUpdateState;
  }

  public void setUserUpdateState(UserUpdateState userUpdateState)
  {
    _userUpdateState = userUpdateState;
  }

  public AuctionUpdateState getAuctionUpdateState()
  {
    return _auctionUpdateState;
  }

  public void setAuctionUpdateState(AuctionUpdateState auctionUpdateState)
  {
    _auctionUpdateState = auctionUpdateState;
  }

  public PaymentState getPaymentState()
  {
    return _paymentState;
  }

  public void setPaymentState(PaymentState paymentState)
  {
    _paymentState = paymentState;
  }

  public boolean isCommitted()
  {
    boolean isCommitted = _commitPhase == CommitPhase.COMMITTING;

    isCommitted &= _userUpdateState == UserUpdateState.SUCCESS;

    isCommitted &= _auctionUpdateState == AuctionUpdateState.SUCCESS;

    isCommitted &= _paymentState == PaymentState.SUCCESS;

    return isCommitted;
  }

  public boolean isCommitting()
  {
    return _commitPhase == CommitPhase.COMMITTING;
  }

  public boolean isRolledBack()
  {
    boolean isRolledBack = _commitPhase == CommitPhase.ROLLING_BACK;

    isRolledBack &= (_userUpdateState == UserUpdateState.REJECTED
                     || _userUpdateState == UserUpdateState.ROLLED_BACK);

    isRolledBack &= (_auctionUpdateState == AuctionUpdateState.REJECTED
                     || _auctionUpdateState == AuctionUpdateState.ROLLED_BACK);

    isRolledBack &= (_paymentState == PaymentState.REFUNDED
                     || _paymentState == PaymentState.FAILED);

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
           + _commitPhase
           + ", "
           + _userUpdateState + ", "
           + _auctionUpdateState + ", "
           + _paymentState
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

  enum PaymentState
  {
    SUCCESS,
    FAILED,
    REFUNDED,
    UNKNOWN
  }
}
