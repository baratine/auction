package examples.auction.s1;

import examples.auction.AuctionDataPublic;
import examples.auction.PayPal;
import io.baratine.core.Journal;
import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.stream.ResultStreamBuilderSync;

@Journal()
public class AuctionSettlementImpl
  implements AuctionSettlement, AuctionSettlementInternal
{
  private DatabaseService _db;
  private PayPal _payPal;

  private BoundState _boundState = BoundState.UNBOUND;
  private String _id;
  private String _auctionId;
  private String _userId;
  private AuctionDataPublic.Bid _bid;
  private TransactionState _state;

  private AuctionSettlementInternal _self;

  public AuctionSettlementImpl(String id)
  {
    _id = id;
  }

  //id, auction_id, user_id, bid
  @OnLoad
  public void load()
  {
    if (_boundState != BoundState.UNBOUND)
      throw new IllegalStateException();

    //xxx: refactor to async when Result.fork() is reworked.
    Cursor settelment = ((ResultStreamBuilderSync<Cursor>) (_db.findLocal(
      "select auction_id, user_id, bid from settlement where id = ?",
      _id).first())).result();

    Cursor state = ((ResultStreamBuilderSync<Cursor>) (_db.findLocal(
      "select state from settlement_state where id = ?",
      _id).first())).result();

    load(settelment, state);
  }

  public void load(Cursor settlement, Cursor state)
  {
    if (settlement == null)
      return;

    _auctionId = settlement.getString(1);

    _userId = settlement.getString(2);

    _bid = (AuctionDataPublic.Bid) settlement.getObject(3);

    if (state != null)
      _state = (TransactionState) state.getObject(1);

    _boundState = BoundState.BOUND;
  }

  @Modify
  @Override
  public void create(String auctionId,
                     String userId,
                     AuctionDataPublic.Bid bid,
                     Result<Boolean> result)
  {
    if (_boundState != BoundState.UNBOUND)
      throw new IllegalStateException();

    _auctionId = auctionId;
    _userId = userId;
    _bid = bid;

    _boundState = BoundState.NEW;
  }

  @Override
  @Modify
  public void commit(Result<Status> status)
  {
    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException();

    if (_state == null)
      _state = new TransactionState();

    _self.commitImpl(status);
  }

  @Override
  public void commitImpl(Result<Status> status)
  {
    TransactionState.CommitState commitState = _state.getCommitState();
    TransactionState.RollbackState rollbackState = _state.getRollbackState();

    if (rollbackState != null)
      throw new IllegalStateException();

    switch (commitState) {
    case COMPLETED: {
      status.complete(Status.COMMITTED);
      break;
    }
    case PENDING: {
      commitPending(status);
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

  public void commitPending(Result<Status> status)
  {
    _payPal.settle();
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

  enum BoundState
  {
    UNBOUND,
    NEW,
    BOUND
  }
}

interface AuctionSettlementInternal extends AuctionSettlement
{
  void commitImpl(Result<Status> status);
}
