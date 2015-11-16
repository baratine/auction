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
  private SettlementStatus _status;
  private SettlementIntent _intent;

  private AuctionSettlementInternal _self;

  public AuctionSettlementImpl(String id)
  {
    _id = id;
  }

  //id, auction_id, user_id, bid
  @OnLoad
  public void load()
  {
    //xxx: refactor to async when Result.fork() is reworked.
    Cursor settelment = ((ResultStreamBuilderSync<Cursor>) (_db.findLocal(
      "select auction_id, user_id, bid from settlement where id = ?",
      _id).first())).result();

    Cursor intent = ((ResultStreamBuilderSync<Cursor>) (_db.findLocal(
      "select intent from settlement_intent where id = ?",
      _id).first())).result();

    Cursor status = ((ResultStreamBuilderSync<Cursor>) (_db.findLocal(
      "select status from settlement_status where id = ?",
      _id).first())).result();

    load(settelment, intent, status);
  }

  public void load(Cursor settlement, Cursor intent, Cursor status)
  {
    if (settlement == null)
      return;

    _auctionId = settlement.getString(1);

    _userId = settlement.getString(2);

    _bid = (AuctionDataPublic.Bid) settlement.getObject(3);

    if (intent != null)
      _intent = (SettlementIntent) intent.getObject(1);

    if (status != null)
      _status = (SettlementStatus) status.getObject(1);

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
    _status = null;

    _boundState = BoundState.NEW;
  }

  @Override
  @Modify
  public void settle(Result<SettlementStatus> status)
  {
    if (_intent != null)
      _intent.verifyIntent(SettlementIntent.SETTLE);

    if (_status != null)
      _status.verifyIntent(SettlementIntent.SETTLE);

    if (_status != null && _status.isFinite()) {
      status.complete(_status);

      return;
    }

    if (_intent == null) {
      _intent = SettlementIntent.SETTLE;

      _self.settleImpl(status);
    }
  }

  @Override
  public void settleImpl(Result<SettlementStatus> status)
  {
    _status = SettlementStatus.PENDING;


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
             _intent);
    _db.exec("insert into settlement_status (id, status) values (?, ?)",
             x -> {},
             _id,
             _status);
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
  void settleImpl(Result<SettlementStatus> status);
}