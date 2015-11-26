package examples.auction.s1;

import examples.auction.Auction;
import examples.auction.AuctionDataPublic;
import examples.auction.CreditCard;
import examples.auction.PayPal;
import examples.auction.Payment;
import examples.auction.Refund;
import examples.auction.User;
import examples.auction.s1.TransactionState.AuctionUpdateState;
import examples.auction.s1.TransactionState.UserUpdateState;
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

    if (_state == null)
      _state = new TransactionState();

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

    commitImpl(status);
  }

  public void commitImpl(Result<Status> status)
  {
    if (_state.isRollingBack())
      throw new IllegalStateException();
    else if (_state.isCommitted())
      throw new IllegalStateException();
    else if (!_state.isCommitting())
      throw new IllegalStateException();

    commitPending(status.from(x -> processCommit(x)));
  }

  public void commitPending(Result<Boolean> status)
  {
    Result<Boolean>[] children
      = status.fork(3, (s, r) -> r.complete(s.get(0) && s.get(1) && s.get(2)),
                    (s, e, r) -> r.complete(false));

    updateUser(children[0]);
    updateAuction(children[1]);
    chargeUser(children[2]);
  }

  public void updateUser(Result<Boolean> status)
  {
    User user = getUser();

    user.addWonAuction(_auctionId,
                       status.from(x -> {
                         _state.setUserUpdateState(x ?
                                                     UserUpdateState.SUCCESS :
                                                     UserUpdateState.REJECTED);
                         return x;
                       }));
  }

  public void updateAuction(Result<Boolean> status)
  {
    Auction auction = getAuction();

    auction.setPendingAuctionWinner(_userId, status.from(x -> {
      _state.setAuctionUpdateState(
        x ? AuctionUpdateState.SUCCESS : AuctionUpdateState.REJECTED);
      return x;
    }));
  }

  public void chargeUser(Result<Boolean> status)
  {
    User user = getUser();
    Auction auction = getAuction();

    final ValueRef<AuctionDataPublic> auctionData = new ValueRef();
    final ValueRef<CreditCard> creditCard = new ValueRef();

    Result<Boolean> fork = Result.from(x ->
                                         chargeUser(auctionData.get(),
                                                    creditCard.get(),
                                                    status),
                                       e -> status.complete(false)
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
                         Result<Boolean> status)
  {
    _payPal.settle(auctionData,
                   _bid,
                   creditCard,
                   _userId,
                   _id,
                   status.from(x -> processPayment(x)));

  }

  private boolean processPayment(Payment payment)
  {
    _state.setPayment(payment);

    if (payment.getState().equals(PaymentState.approved)) {
      _state.setPaymentState(TransactionState.PaymentState.SUCCESS);

      return true;
    }
    else {
      _state.setPaymentState(TransactionState.PaymentState.FAILED);

      return false;
    }
  }

  private Status processCommit(boolean result)
  {
    if (result) {
      getAuction().setSettled(Result.ignore());

      return Status.COMMITTED;
    }
    else {
      _state.toRollBack();

      return Status.ROLLING_BACK;
    }
  }

  @Modify
  @Override
  public void rollback(Result<Status> status)
  {
    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException();

    if (_state.isRollingBack())
      rollbackImpl(status);
  }

  public void rollbackImpl(Result<Status> status)
  {
    rollbackPending(status.from(x -> {
      if (x) {
        return Status.ROLLED_BACK;
      }
      else {
        //process false
        return Status.ROLLED_BACK;
      }
    }));
  }

  public void rollbackPending(Result<Boolean> status)
  {
    Result<Boolean>[] children
      = status.fork(3, (s, r) -> r.complete(true),
                    (s, e, r) -> {});

    resetUser(children[0]);
    resetAuction(children[1]);
    refundUser(children[2]);
  }

  private void resetUser(Result<Boolean> result)
  {
    if (_state.getUserUpdateState() == UserUpdateState.REJECTED) {
      result.complete(true);

      return;
    }

    User user = getUser();
    user.removeWonAuction(_auctionId,
                          result.from(x -> {
                            if (x) {
                              _state.setUserUpdateState(
                                UserUpdateState.ROLLED_BACK);
                              return true;
                            }
                            else {
                              throw new IllegalStateException();
                            }
                          }));
  }

  private void resetAuction(Result<Boolean> result)
  {
    if (_state.getAuctionUpdateState() == AuctionUpdateState.REJECTED) {
      result.complete(true);

      return;
    }

    Auction auction = getAuction();

    auction.clearAuctionWinner(_userId,
                               result.from(x -> {
                                 if (x) {
                                   _state.setAuctionUpdateState(
                                     AuctionUpdateState.ROLLED_BACK);
                                   return true;
                                 }
                                 else {
                                   throw new IllegalStateException();
                                 }
                               }));
  }

  private void refundUser(Result<Boolean> result)
  {
    if (_state.getPaymentState() == TransactionState.PaymentState.FAILED) {
      result.complete(true);

      return;
    }

    Payment payment = _state.getPayment();
    if (payment == null) {
      //send payment to refund service
      result.complete(true);
    }
    else {
      _payPal.refund(_id, payment.getSaleId(),
                     payment.getSaleId(),
                     result.from(r -> processRefund(r)));
    }
  }

  private boolean processRefund(Refund refund)
  {
    if (refund.getStatus() == Refund.RefundState.completed) {
      _state.setPaymentState(TransactionState.PaymentState.REFUNDED);

      return true;
    }
    else {

      return false;
    }
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
  public void status(Result<Status> result)
  {
    Status status;

    if (_state.isCommitted()) {
      status = Status.COMMITTED;
    }
    else if (_state.isRolledBack()) {
      status = Status.ROLLED_BACK;
    }
    else if (_state.isCommitting()) {
      status = Status.COMMITTING;
    }
    else if (_state.isRollingBack()) {
      status = Status.ROLLING_BACK;
    }
    else {
      throw new IllegalStateException(_state.toString());
    }

    result.complete(status);
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
