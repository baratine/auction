package examples.auction.s1;

import examples.auction.Auction;
import examples.auction.AuctionDataPublic;
import examples.auction.CreditCard;
import examples.auction.PayPal;
import examples.auction.Payment;
import examples.auction.Refund;
import examples.auction.User;
import io.baratine.core.Journal;
import io.baratine.core.Modify;
import io.baratine.core.OnInit;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.core.ServiceManager;
import io.baratine.core.ServiceRef;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;

import java.util.logging.Logger;

import static examples.auction.Payment.PaymentState;
import static examples.auction.s1.TransactionState.CommitState;
import static examples.auction.s1.TransactionState.RollbackState;

@Journal()
public class AuctionSettlementImpl
  implements AuctionSettlement
{
  private final static Logger log
    = Logger.getLogger(AuctionSettlementImpl.class.getName());

  private DatabaseService _db;
  private PayPal _payPal;
  private ServiceRef _userManager;
  private ServiceRef _auctionManager;

  private BoundState _boundState = BoundState.UNBOUND;

  private String _id;
  private String _auctionId;
  private String _userId;
  private AuctionDataPublic.Bid _bid;

  private TransactionState _state;

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

    Result<Boolean>[] results = result.fork(2, (l -> l.get(0) && l.get(1)));

    _db.findLocal(
      "select auction_id, user_id, bid from settlement where id = ?",
      _id).first().result(results[0].from(c -> loadSettlement(c)));

    _db.findLocal("select state from settlement_state where id = ?",
                  _id).first().result(results[1].from(c -> loadState(c)));
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
      _state = (TransactionState) state.getObject(1);

    return true;
  }

  @OnInit
  public void init(Result<Boolean> result)
  {
    ServiceManager manager = ServiceManager.current();

    _db = manager.lookup("bardb:///").as(DatabaseService.class);

    _payPal = manager.lookup("pod://auction/paypal").as(PayPal.class);
    _userManager = manager.lookup("pod://user/user");
    _auctionManager = manager.lookup("pod://auction/auction");

    result.complete(true);
  }

  @Override
  @Modify
  public void create(String auctionId,
                     String userId,
                     AuctionDataPublic.Bid bid,
                     Result<Boolean> result)
  {
    log.finer(String.format("create %1$s", this));

    if (_boundState != BoundState.UNBOUND)
      throw new IllegalStateException();

    _auctionId = auctionId;
    _userId = userId;
    _bid = bid;

    _boundState = BoundState.NEW;

    result.complete(true);
  }

  @Modify
  @Override
  public void commit(Result<Status> status)
  {
    log.finer(String.format("commit settlement %1$s", this));

    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException();

    if (_state == null)
      _state = new TransactionState();

    commitImpl(status);
  }

  @Modify
  @Override
  public void rollback(Result<Status> status)
  {
    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException();

    if (_state == null)
      throw new IllegalStateException();

    if (_state.getRollbackState() == null)
      _state.setRollbackState(RollbackState.PENDING);

    rollbackImpl(status);
  }

  public void commitImpl(Result<Status> status)
  {
    CommitState commitState = _state.getCommitState();
    RollbackState rollbackState = _state.getRollbackState();

    if (rollbackState != null)
      throw new IllegalStateException();

    switch (commitState) {
    case COMPLETED: {
      status.complete(Status.COMMITTED);
      break;
    }
    case PENDING: {
      commitPending(status.from(x -> processCommit(x)));
      break;
    }
    case REJECTED_PAYMENT: {
      status.complete(Status.ROLLING_BACK);
      break;
    }
    case REJECTED_USER: {
      status.complete(Status.ROLLING_BACK);
      break;
    }
    case REJECTED_AUCTION: {
      status.complete(Status.ROLLING_BACK);
      break;
    }
    default: {
      break;
    }
    }
  }

  public void commitPending(Result<TransactionState.CommitState> status)
  {
    Result<TransactionState.CommitState>[] children
      = status.fork(3, (s, r) -> r.complete(
                      TransactionState.CommitState.COMPLETED),
                    (s, e, r) -> {});

    updateUser(children[0]);
    updateAuction(children[1]);
    chargeUser(children[2]);
  }

  public void updateUser(Result<TransactionState.CommitState> status)
  {
    User user = getUser();

    user.addWonAuction(_auctionId,
                       status.from(x -> x ?
                         CommitState.COMPLETED :
                         CommitState.REJECTED_USER));
  }

  public void updateAuction(Result<CommitState> status)
  {
    Auction auction = getAuction();

    auction.setAuctionWinner(_userId, status.from(x -> x ?
      CommitState.COMPLETED :
      CommitState.REJECTED_AUCTION));
  }

  public void chargeUser(Result<CommitState> status)
  {
    User user = getUser();
    Auction auction = getAuction();

    final ValueRef<AuctionDataPublic> auctionData = new ValueRef();
    final ValueRef<CreditCard> creditCard = new ValueRef();

    Result<Boolean> fork = Result.from(x ->
                                         chargeUser(auctionData.get(),
                                                    creditCard.get(),
                                                    status),
                                       e -> status.complete(CommitState.REJECTED_USER)
    );

    Result<Boolean>[] forked
      = fork.fork(2, (x, r) -> r.complete(x.get(0) && x.get(1)));

    auction.get(forked[0].from(d -> {
      auctionData.set(d);
      return d != null;
    }));

    user.getCreditCard(forked[1].from(c -> {
      creditCard.set(c);
      return c != null;
    }));
  }

  public void chargeUser(AuctionDataPublic auctionData,
                         CreditCard creditCard,
                         Result<CommitState> status)
  {
    _payPal.settle(auctionData,
                   _bid,
                   creditCard,
                   _userId,
                   _id,
                   status.from(x -> processPayment(x)));

  }

  private CommitState processPayment(Payment payment)
  {
    _state.setPayment(payment);

    if (payment.getState().equals(PaymentState.approved)) {
      return CommitState.COMPLETED;
    }
    else {
      return CommitState.REJECTED_PAYMENT;
    }
  }

  private Status processCommit(CommitState commitState)
  {
    _state.setCommitState(commitState);

    switch (commitState) {
    case COMPLETED: {
      return Status.COMMITTED;
    }
    case PENDING: {
      return Status.PENDING;
    }
    case REJECTED_AUCTION:
    case REJECTED_PAYMENT:
    case REJECTED_USER: {
      return Status.ROLLING_BACK;
    }
    default: {
      throw new IllegalStateException();
    }
    }
  }

  public void rollbackImpl(Result<Status> status)
  {
    RollbackState rollbackState = _state.getRollbackState();

    switch (rollbackState) {
    case COMPLETED: {
      status.complete(Status.ROLLED_BACK);
      break;
    }
    case PENDING: {
      rollbackPending(status.from(x -> processRollback(x)));
      break;
    }
    case REFUND_FAILED: {
      //TODO:
      break;
    }
    default: {
      throw new IllegalStateException();
    }
    }
  }

  private Status processRollback(RollbackState state)
  {
    switch (state) {
    case COMPLETED:
      return Status.ROLLED_BACK;
    case PENDING:
      return Status.ROLLING_BACK;
    default: {
      throw new IllegalStateException();
    }
    }
  }

  public void rollbackPending(Result<TransactionState.RollbackState> status)
  {
    Result<TransactionState.RollbackState>[] forked
      = status.fork(3, (s, r) -> r.complete(
                      TransactionState.RollbackState.COMPLETED),
                    (s, e, r) -> {});

    resetUser(forked[0]);
    resetAuction(forked[1]);
    refundUser(forked[2]);
  }

  private void resetUser(Result<RollbackState> forkedState)
  {
    User user = getUser();
    user.removeWonAuction(_auctionId,
                          forkedState.from(x -> RollbackState.COMPLETED));
  }

  private void resetAuction(Result<RollbackState> forkedState)
  {
    Auction auction = getAuction();

    auction.resetAuctionWinner(_userId,
                               forkedState.from(x -> RollbackState.COMPLETED));
  }

  private void refundUser(Result<RollbackState> forkedState)
  {
    Payment payment = _state.getPayment();
    if (payment == null) {
      forkedState.complete(RollbackState.COMPLETED);
    }
    else {
      _payPal.refund(_id, payment.getSaleId(),
                     payment.getSaleId(),
                     forkedState.from(r -> processRefund(r)));
    }
  }

  private RollbackState processRefund(Refund refund)
  {
    _state.setRefund(refund);

    switch (refund.getStatus()) {
    case completed: {
      break;
    }
    case failed: {
      break;
    }
    case pending: {
      break;
    }
    }

    return RollbackState.COMPLETED;
  }

  private User getUser()
  {
    User user = _userManager.lookup('/' + _userId).as(User.class);

    return user;
  }

  private Auction getAuction()
  {
    Auction auction = _auctionManager.lookup('/' + _auctionId)
                                     .as(Auction.class);

    return auction;
  }

  @OnSave
  public void save()
  {
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
             x -> {
             },
             _id,
             _state);
  }

  @Override
  public void status(Result<Status> status)
  {
    CommitState commitState = _state.getCommitState();
    RollbackState rollbackState = _state.getRollbackState();

    if (rollbackState == null) {
      switch (commitState) {
      case COMPLETED: {
        status.complete(Status.COMMITTED);
        break;
      }
      case PENDING: {
        status.complete(Status.PENDING);
        break;
      }
      case REJECTED_AUCTION:
      case REJECTED_PAYMENT:
      case REJECTED_USER: {
        status.complete(Status.ROLLING_BACK);
        break;
      }
      default: {
        throw new IllegalStateException();
      }
      }
    }
    else {
      switch (rollbackState) {
      case COMPLETED: {
        status.complete(Status.ROLLED_BACK);
        break;
      }
      case PENDING: {
        status.complete(Status.ROLLING_BACK);
        break;
      }
      }
    }
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
