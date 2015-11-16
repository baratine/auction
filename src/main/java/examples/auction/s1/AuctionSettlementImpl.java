package examples.auction.s1;

import examples.auction.AuctionDataPublic;
import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.stream.ResultStreamBuilderSync;

import javax.inject.Inject;

@Journal()
public class AuctionSettlementImpl
  implements AuctionSettlement, AuctionSettlementInternal
{
  @Inject
  @Lookup("bardb:///")
  DatabaseService _db;

  private BoundState _boundState = BoundState.UNBOUND;
  private String _id;
  private String _auctionId;
  private String _userId;
  private AuctionDataPublic.Bid _bid;
  private SettlementState _state;

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
      _state = (SettlementState) state.getObject(1);

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
  public void settle(Result<SettlementState.ActionStatus> status)
  {
    if (_boundState == BoundState.UNBOUND)
      throw new IllegalStateException();

    if (_state == null) {
      _state = new SettlementState(SettlementState.Action.SETTLE);
    } else if (_state.isSettled()) {

    }
    else if (_state.toSettle()) {

    }



    if (_action != null)
      _action.verifyIntent(SettlementState.Action.SETTLE);

    if (_actionStatus != null)
      _actionStatus.verifyIntent(SettlementState.Action.SETTLE);

    if (_actionStatus != null && _actionStatus.isFinite()) {
      status.complete(_actionStatus);

      return;
    }

    if (_action == null) {
      _action = SettlementState.Action.SETTLE;

      _self.settleImpl(status);
    }
  }

  @Override
  public void settleImpl(Result<SettlementState.ActionStatus> status)
  {
    _actionStatus = SettlementState.ActionStatus.PENDING;

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

    _db.exec("insert into settlement_intent (id, intent) values (?, ?)",
             x -> {},
             _id,
             _action);
    _db.exec("insert into settlement_status (id, status) values (?, ?)",
             x -> {},
             _id,
             _actionStatus);
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
  void settleImpl(Result<SettlementState.ActionStatus> status);
}