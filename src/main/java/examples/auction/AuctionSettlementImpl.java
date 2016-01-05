package examples.auction;

import examples.auction.SettlementTransactionState.AuctionUpdateState;
import examples.auction.SettlementTransactionState.AuctionWinnerUpdateState;
import examples.auction.SettlementTransactionState.PaymentTxState;
import examples.auction.SettlementTransactionState.UserUpdateState;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.service.Modify;
import io.baratine.service.OnLoad;
import io.baratine.service.OnSave;
import io.baratine.service.Result;
import io.baratine.service.ServiceRef;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionSettlementImpl implements AuctionSettlement
{
  private final static Logger log
    = Logger.getLogger(AuctionSettlementImpl.class.getName());

  private final DatabaseService _db;
  private final ServiceRef _userManager;
  private final ServiceRef _auctionManager;
  private final PayPal _paypal;
  private final AuditService _audit;

  private String _id;
  private AuctionDataPublic.Bid _bid;
  private SettlementTransactionState _state;

  private BoundState _boundState = BoundState.UNBOUND;

  private boolean _inProgress = false;

  public AuctionSettlementImpl(String id,
                               AuctionSettlementManagerImpl settlementManager)
  {
    _id = id;
    _db = settlementManager.getDatabase();
    _paypal = settlementManager.getPayPal();
    _audit = settlementManager.getAuditService();
    _auctionManager = settlementManager.getAuctionManager();
    _userManager = settlementManager.getUserManager();
  }

  //id, auction_id, user_id, bid
  @OnLoad
  public void load(Result<Boolean> result)
  {
    if (_boundState != BoundState.UNBOUND)
      throw new IllegalStateException();

    Result.Fork<Boolean,Boolean> fork = result.fork();

/*
    _db.findLocal(
      "select auction_id, user_id, bid from settlement where id = ?",
      _id).first().result(fork.fork().of(c -> loadSettlement(c)));

    _db.findLocal("select state from settlement_state where id = ?",
                  _id).first().result(fork.fork().of(c -> loadState(c)));
*/

    _db.findOne("select state from settlement_state where id = ?",
                fork.branch().of(c -> loadState(c)), _id);

    _db.findOne("select bid from settlement where id = ?",
                fork.branch().of(c -> loadSettlement(c)), _id);

    fork.join(l -> l.get(0) && l.get(1));
  }

  public boolean loadSettlement(Cursor settlement)
  {
    if (settlement != null) {
      _bid = (AuctionDataPublic.Bid) settlement.getObject(1);

      _boundState = BoundState.BOUND;
    }

    return true;
  }

  public boolean loadState(Cursor state)
  {
    if (state != null)
      _state = (SettlementTransactionState) state.getObject(1);

    if (_state == null)
      _state = new SettlementTransactionState();

    return true;
  }

  @Override
  @Modify
  public void settle(AuctionDataPublic.Bid bid,
                     Result<Status> status)
  {
    log.finer(String.format("create %1$s", this));

    if (_boundState != BoundState.UNBOUND)
      throw new IllegalStateException();

    _bid = bid;

    _boundState = BoundState.NEW;

    log.finer(String.format("settle %1$s", this));

    settleImpl(status);
  }

  @Override
  public void settleResume(Result<Status> status)
  {
    if (_state.isSettled()) {
      status.ok(Status.SETTLED);
    }
    else if (_state.isRefunded()) {
      throw new IllegalStateException();
    }
    else if (_state.isRefunding()) {
      throw new IllegalStateException();
    }

    settleImpl(status);
  }

  private void settleImpl(Result<Status> status)
  {
    if (_state.getSettleStatus() == Status.SETTLE_FAILED) {
      status.ok(_state.getSettleStatus());
    }
    else if (_inProgress) {
      status.ok(_state.getSettleStatus());
    }
    else {
      _inProgress = true;

      settlePending(status);
    }
  }

  public void settlePending(Result<Status> status)
  {
    Result.Fork<Boolean,Status> fork = status.fork();

    fork.fail((l, t, r) -> this.settleFail(r));

    updateUser(fork.branch());
    updateAuction(fork.branch());
    chargeUser(fork.branch());

    fork.join((l, r) -> {
      boolean isSuccess = l.get(0) && l.get(1) && l.get(2);

      if (isSuccess) {
        settleComplete(r);
      }
      else {
        settleFail(r);
      }
    });
  }

  public void updateUser(Result<Boolean> status)
  {
    if (_state.getUserSettleState() == UserUpdateState.SUCCESS) {
      status.ok(true);
    }
    else {
      getWinner().addWonAuction(_bid.getAuctionId(),
                                status.of(x -> afterUserUpdated(x)));
    }
  }

  private boolean afterUserUpdated(boolean isAccepted)
  {
    if (isAccepted) {
      _state.setUserSettleState(UserUpdateState.SUCCESS);
      //audit
    }
    else {
      _state.setUserSettleState(UserUpdateState.REJECTED);
      //audit
    }

    return isAccepted;
  }

  private User getWinner()
  {
    return _userManager.lookup('/' + _bid.getUserId()).as(User.class);
  }

  public void updateAuction(Result<Boolean> status)
  {
    if (_state.getAuctionWinnerUpdateState()
        == AuctionWinnerUpdateState.SUCCESS) {
      status.ok(true);
    }
    else {
      getAuction().setAuctionWinner(_bid.getUserId(),
                                    status.of(x -> afterAuctionUpdated(x)));
    }
  }

  private boolean afterAuctionUpdated(boolean isAccepted)
  {
    if (isAccepted) {
      _state.setAuctionWinnerUpdateState(AuctionWinnerUpdateState.SUCCESS);
      //audit
    }
    else {
      _state.setAuctionWinnerUpdateState(AuctionWinnerUpdateState.REJECTED);
      //audit
    }

    return isAccepted;
  }

  private Auction getAuction()
  {
    return _auctionManager.lookup('/' + _bid.getAuctionId()).as(Auction.class);
  }

  public void chargeUser(Result<Boolean> status)
  {
    final ValueRef<AuctionDataPublic> auctionData = new ValueRef();
    final ValueRef<CreditCard> creditCard = new ValueRef();

    Result<Boolean> paymentResult = Result.of(x ->
                                                chargeUser(auctionData.get(),
                                                           creditCard.get(),
                                                           status),
                                              e -> status.ok(false)
    );

    Result.Fork<Boolean,Boolean> fork = paymentResult.fork();

    getAuction().get(fork.branch().of(a -> {
      auctionData.set(a);
      return a != null;
    }));

    getWinner().getCreditCard(fork.branch().of(c -> {
      creditCard.set(c);
      return c != null;
    }));

    fork.join(l -> l.get(0) && l.get(1));
  }

  public void chargeUser(AuctionDataPublic auctionData,
                         CreditCard creditCard,
                         Result<Boolean> status)
  {
    _paypal.settle(auctionData,
                   _bid,
                   creditCard,
                   _id,
                   status.of(x -> processPayment(x)));
  }

  private boolean processPayment(Payment payment)
  {
    _state.setPayment(payment);

    boolean result;

    if (payment.getState().equals(Payment.PaymentState.approved)) {
      _state.setPaymentState(PaymentTxState.SUCCESS);

      //audit

      result = true;
    }
    else if (payment.getState().equals(Payment.PaymentState.pending)) {
      _state.setPaymentState(PaymentTxState.PENDING);

      //audit

      result = false;
    }
    else {
      _state.setPaymentState(PaymentTxState.FAILED);

      //audit

      result = false;
    }

    return result;
  }

  private void settleComplete(Result<Status> result)
  {
    getAuction().setSettled(result.of((x, r) -> {
      _state.setAuctionStateUpdateState(AuctionUpdateState.SUCCESS);
      _state.setSettleStatus(Status.SETTLED);
      r.ok(Status.SETTLED);
      _inProgress = false;
    }));
  }

  private void settleFail(Result<Status> result)
  {
    Status status = Status.SETTLING;

    if (_state.getUserSettleState() == UserUpdateState.REJECTED) {
      status = Status.SETTLE_FAILED;
      //audit
    }
    else if (_state.getAuctionWinnerUpdateState()
             == AuctionWinnerUpdateState.REJECTED) {
      status = Status.SETTLE_FAILED;
      //audit
    }
    else if (_state.getPaymentState() == PaymentTxState.FAILED) {
      status = Status.SETTLE_FAILED;
      //audit
    }

    //audit
    _state.setSettleStatus(status);

    _inProgress = false;

    if (Status.SETTLE_FAILED == status) {
      refund(result);
    }
    else {
      result.ok(status);
    }
  }

  @Modify
  @Override
  public void refund(Result<Status> status)
  {
    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException();

    if (_state.isRefunded()) {
      status.ok(Status.ROLLED_BACK);
    }
    else if (_state.isCommitting() && _inProgress) {
      throw new IllegalStateException();
    }

    _state.toRefund();

    refundImpl(status);
  }

  public void refundImpl(Result<Status> status)
  {
    if (_inProgress) {
      status.ok(_state.getRefundStatus());
    }
    else {
      refundPending(status.of(x -> processRefund(x)));
    }
  }

  public void refundPending(Result<Boolean> status)
  {
    Result.Fork<Boolean,Boolean> fork = status.fork();

    resetUser(fork.branch());
    resetAuction(fork.branch());
    refundUser(fork.branch());

    fork.join(l -> l.get(0) && l.get(1) && l.get(2));
  }

  private Status processRefund(boolean result)
  {
    Status status = Status.ROLLING_BACK;

    if (result) {
      status = Status.ROLLED_BACK;
      getAuction().setRolledBack((b, t) -> _state.setRefundStatus(Status.ROLLED_BACK));
    }

    _state.setRefundStatus(status);

    return status;
  }

  private void resetUser(Result<Boolean> result)
  {
    if (_state.getUserSettleState()
        == UserUpdateState.REJECTED) {
      result.ok(true);
    }
    else if (_state.getUserResetState() == UserUpdateState.ROLLED_BACK) {
      result.ok(true);
    }
    else {
      getWinner().removeWonAuction(_bid.getAuctionId(),
                                   result.of(x -> afterUserReset(x)));
    }
  }

  private boolean afterUserReset(boolean isReset)
  {
    if (isReset) {
      _state.setUserResetState(UserUpdateState.ROLLED_BACK);
      //audit
    }
    else {
      throw new IllegalStateException();
    }

    return isReset;
  }

  private void resetAuction(Result<Boolean> result)
  {
    if (_state.getAuctionWinnerUpdateState()
        == AuctionWinnerUpdateState.REJECTED) {
      result.ok(true);
    }
    else if (_state.getAuctionWinnerResetState()
             == AuctionWinnerUpdateState.ROLLED_BACK) {
      result.ok(true);
    }
    else {
      getAuction().clearAuctionWinner(_bid.getUserId(),
                                      result.of(x -> afterAuctionReset(x)));
    }
  }

  private boolean afterAuctionReset(boolean isReset)
  {
    if (isReset) {
      _state.setAuctionWinnerResetState(AuctionWinnerUpdateState.ROLLED_BACK);

      //audit
    }
    else {
      throw new IllegalStateException();
    }

    return isReset;
  }

  private void refundUser(Result<Boolean> result)
  {
    if (_state.getPaymentState() == PaymentTxState.FAILED) {
      result.ok(true);
    }
    else if (_state.getRefundState() == PaymentTxState.REFUNDED) {
      result.ok(true);
    }
    else {
      Payment payment = _state.getPayment();

      if (payment == null) {

        result.ok(true);
      }
      else {
        payPalRefund(payment, result);
      }
    }
  }

  private void payPalRefund(Payment payment, Result<Boolean> result)
  {
    _paypal.refund(_id, payment.getSaleId(), payment.getSaleId(),
                   result.of(refund -> processRefund(refund)));
  }

  private boolean processRefund(Refund refund)
  {
    boolean isRefunded = false;

    if (refund != null)
      _state.setRefund(refund);

    //audits
    if (refund.getStatus() == RefundImpl.RefundState.completed) {
      _state.setRefundState(PaymentTxState.REFUNDED);
      isRefunded = true;
    }

    return isRefunded;
  }

  @OnSave
  public void save()
  {
    log.log(Level.FINER, String.format("saving %1$s", this));

    //id , user_id , bid
    if (_boundState == BoundState.NEW) {
      _db.exec(
        "insert into settlement (id, bid) values (?, ?)",
        (x, t) -> _boundState = BoundState.BOUND,
        _id,
        _bid);
    }

    _db.exec("insert into settlement_state (id, state) values (?, ?)",
             Result.ignore(),
             _id,
             _state);
  }

  @Override
  public void getTransactionState(Result<SettlementTransactionState> result)
  {
    result.ok(_state);
  }

  @Override
  public void settleStatus(Result<Status> result)
  {
    result.ok(_state.getSettleStatus());
  }

  @Override
  public void refundStatus(Result<Status> result)
  {
    result.ok(_state.getRefundStatus());
  }

  @Override
  public String toString()
  {
    return this.getClass().getSimpleName()
           + "["
           + _id
           + ", "
           + _boundState
           + ", "
           + _state
           + "]";
  }

  enum BoundState
  {
    UNBOUND,
    NEW,
    BOUND
  }
}

class ValueRef<T>
{
  private T _t;

  T get()
  {
    return _t;
  }

  T set(T t)
  {
    _t = t;

    return t;
  }
}
