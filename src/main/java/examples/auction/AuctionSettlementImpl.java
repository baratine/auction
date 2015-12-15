package examples.auction;

import examples.auction.SettlementTransactionState.AuctionWinnerUpdateState;
import examples.auction.SettlementTransactionState.PaymentTxState;
import examples.auction.SettlementTransactionState.UserUpdateState;
import io.baratine.core.Modify;
import io.baratine.core.OnInit;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.core.ServiceManager;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionSettlementImpl implements AuctionSettlement
{
  private final static Logger log
    = Logger.getLogger(AuctionSettlementImpl.class.getName());

  private DatabaseService _db;
  private AuctionSettlementManager _settlementManager;

  private BoundState _boundState = BoundState.UNBOUND;

  private String _id;
  private String _auctionId;
  private String _userId;
  private AuctionDataPublic.Bid _bid;

  private SettlementTransactionState _state;

  private boolean _inProgress = false;

  public AuctionSettlementImpl(String id)
  {
    _id = id;
  }

  //id, auction_id, user_id, bid
  @OnLoad
  public void load(Result<Boolean> result)
  {
    if (_boundState != BoundState.UNBOUND)
      throw new IllegalStateException();

    Result.Fork<Boolean,Boolean> fork = result.newFork();

    fork.fail((l, e, r) -> {
      for (Throwable t : e) {
        if (t != null) {
          r.fail(t);

          break;
        }
      }
    });

/*
    _db.findLocal(
      "select auction_id, user_id, bid from settlement where id = ?",
      _id).first().result(fork.fork().from(c -> loadSettlement(c)));

    _db.findLocal("select state from settlement_state where id = ?",
                  _id).first().result(fork.fork().from(c -> loadState(c)));
*/

    _db.findOne("select state from settlement_state where id = ?",
                fork.fork().from(c -> loadState(c)), _id);

    _db.findOne("select auction_id, user_id, bid from settlement where id = ?",
                fork.fork().from(c -> loadSettlement(c)), _id);

    fork.join(l -> l.get(0) && l.get(1));
  }

  public boolean loadSettlement(Cursor settlement)
  {
    if (settlement != null) {
      _auctionId = settlement.getString(1);

      _userId = settlement.getString(2);

      _bid = (AuctionDataPublic.Bid) settlement.getObject(3);

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

  @OnInit
  public void init(Result<Boolean> result)
  {
    ServiceManager manager = ServiceManager.current();

    _db = manager.lookup("bardb:///").as(DatabaseService.class);

    _settlementManager = manager.lookup("pod://settlement/settlement")
                                .as(AuctionSettlementManager.class);

    result.complete(true);
  }

  @Override
  @Modify
  public void settle(String auctionId,
                     String userId,
                     AuctionDataPublic.Bid bid,
                     Result<Status> status)
  {
    log.finer(String.format("create %1$s", this));

    if (_boundState != BoundState.UNBOUND)
      throw new IllegalStateException();

    _auctionId = auctionId;
    _userId = userId;
    _bid = bid;

    _boundState = BoundState.NEW;

    log.finer(String.format("settle %1$s", this));

    settleImpl(status);
  }

  @Override
  public void settleResume(Result<Status> status)
  {
    if (_state.isSettled()) {
      status.complete(Status.SETTLED);
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
      status.complete(_state.getSettleStatus());
    }
    else if (_inProgress) {
      status.complete(_state.getSettleStatus());
    }
    else {
      _inProgress = true;

      settlePending(status.from((x, r) -> processSettle(x, r)));
    }
  }

  public void settlePending(Result<Boolean> status)
  {
    Result.Fork<Boolean,Boolean> fork = status.newFork();

    fork.fail((x, e, r) -> {
      for (Throwable t : e) {
        log.log(Level.FINER, t.getMessage(), t);

        if (t != null) {
          r.fail(t);

          break;
        }
      }
    });

    updateUser(fork.fork());
    updateAuction(fork.fork());
    chargeUser(fork.fork());

    fork.join(l -> l.get(0) && l.get(1) && l.get(2));
  }

  public void updateUser(Result<Boolean> status)
  {
    if (_state.getUserSettleState() == UserUpdateState.SUCCESS) {
      status.complete(true);
    }
    else {
      getUser(status.from((u, r) -> {
        u.addWonAuction(_auctionId, r.from(x -> afterUserUpdated(x)));
      }));
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

  private void getUser(Result<User> user)
  {
    _settlementManager.getUser(_userId, user);
  }

  public void updateAuction(Result<Boolean> status)
  {
    if (_state.getAuctionWinnerUpdateState()
        == AuctionWinnerUpdateState.SUCCESS) {
      status.complete(true);
    }
    else {
      getAuction(status.from((a, r) -> {
        a.setAuctionWinner(_userId, r.from(x -> afterAuctionUpdated(x)));
      }));
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

  private void getAuction(Result<Auction> result)
  {
    _settlementManager.getAuction(_auctionId, result);
  }

  public void chargeUser(Result<Boolean> status)
  {
    final ValueRef<AuctionDataPublic> auctionData = new ValueRef();
    final ValueRef<CreditCard> creditCard = new ValueRef();

    Result<Boolean> paymentResult = Result.from(x ->
                                                  chargeUser(auctionData.get(),
                                                             creditCard.get(),
                                                             status),
                                                e -> status.complete(false)
    );

    Result.Fork<Boolean,Boolean> fork = paymentResult.newFork();

    fork = fork.fail((l, e, r) -> {
      for (Throwable t : e) {
        if (t != null) {
          r.fail(t);
          break;
        }
      }
    });

    getAuction(fork.fork().from((a, r) -> {
      a.get(r.from(d -> {
        auctionData.set(d);
        return d != null;
      }));
    }));

    getUser(fork.fork().from((u, r) -> {
      u.getCreditCard(r.from(c -> {
        creditCard.set(c);
        return c != null;
      }));
    }));

    fork.join(l -> l.get(0) && l.get(1));
  }

  public void chargeUser(AuctionDataPublic auctionData,
                         CreditCard creditCard,
                         Result<Boolean> status)
  {
    _settlementManager.getPayPal(status.from((p, r) -> {
      p.settle(auctionData,
               _bid,
               creditCard,
               _userId,
               _id,
               r.from(x -> processPayment(x)));
    }));
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

  private void processSettle(boolean commitResult, Result<Status> result)
  {
    Status status = Status.SETTLING;

    if (commitResult) {

      getAuction(result.from((a, r) -> {
        a.setSettled(r.from((x, ri) -> {
          _state.setSettleStatus(Status.SETTLED);
          ri.complete(Status.SETTLED);
          _inProgress = false;
        }));
      }));

      return;
      //audit
    }
    else if (_state.getUserSettleState() == UserUpdateState.REJECTED) {
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
  }

  @Modify
  @Override
  public void refund(Result<Status> status)
  {
    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException();

    if (_state.isRefunded()) {
      status.complete(Status.ROLLED_BACK);
    }
    else if (_state.isCommitting() && _inProgress) {
      throw new IllegalStateException();
    }

    _state.toRefund();

    rollbackImpl(status);
  }

  public void rollbackImpl(Result<Status> status)
  {
    if (_inProgress) {
      status.complete(_state.getRefundStatus());
    }
    else {
      rollbackPending(status.from(x -> processRollback(x)));
    }
  }

  public void rollbackPending(Result<Boolean> status)
  {
    Result.Fork<Boolean,Boolean> fork = status.newFork();

    fork.fail((l, e, r) -> {
      for (Throwable t : e) {
        if (t != null) {
          r.fail(t);

          break;
        }
      }
    });

    resetUser(fork.fork());
    resetAuction(fork.fork());
    refundUser(fork.fork());

    fork.join(l -> l.get(0) && l.get(1) && l.get(2));
  }

  private Status processRollback(boolean result)
  {
    Status status = Status.ROLLING_BACK;

    if (result) {
      status = Status.ROLLED_BACK;
      getAuction(a -> a.setRolledBack(b -> _state.setRefundStatus(Status.ROLLED_BACK)));
    }

    _state.setRefundStatus(status);

    return status;
  }

  private void resetUser(Result<Boolean> result)
  {
    if (_state.getUserSettleState()
        == UserUpdateState.REJECTED) {
      result.complete(true);
    }
    else if (_state.getUserResetState() == UserUpdateState.ROLLED_BACK) {
      result.complete(true);
    }
    else {
      getUser(result.from((u, r) -> {
        u.removeWonAuction(_auctionId, r.from(x -> afterUserReset(x)));
      }));
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
      result.complete(true);
    }
    else if (_state.getAuctionWinnerResetState()
             == AuctionWinnerUpdateState.ROLLED_BACK) {
      result.complete(true);
    }
    else {
      getAuction((result.from((a, r) -> {
        a.clearAuctionWinner(_userId, r.from(x -> afterAuctionReset(x)));
      })));
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
      result.complete(true);
    }
    else if (_state.getRefundState() == PaymentTxState.REFUNDED) {
      result.complete(true);
    }
    else {

      Payment payment = _state.getPayment();
      if (payment == null) {
        //send payment to refund service
        result.complete(true);
      }
      else {
        payPalRefund(payment, result);
      }
    }
  }

  private void payPalRefund(Payment payment, Result<Boolean> result)
  {
    _settlementManager.getPayPal(result.from((p, r) -> {
      p.refund(_id, payment.getSaleId(), payment.getSaleId(), r.from(
        refund -> processRefund(refund)));
    }));
  }

  private boolean processRefund(Refund refund)
  {
    boolean isRefunded = false;

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

    //id , auction_id , user_id , bid
    if (_boundState == BoundState.NEW) {
      _db.exec(
        "insert into settlement (id, auction_id, user_id, bid) values (?, ?, ?, ?)",
        x -> _boundState = BoundState.BOUND,
        _id,
        _auctionId,
        _userId,
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
    result.complete(_state);
  }

  @Override
  public void settleStatus(Result<Status> result)
  {
    result.complete(_state.getSettleStatus());
  }

  @Override
  public void refundStatus(Result<Status> result)
  {
    result.complete(_state.getRefundStatus());
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
