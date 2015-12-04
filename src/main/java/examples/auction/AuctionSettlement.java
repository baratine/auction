package examples.auction;

import io.baratine.core.Result;

public interface AuctionSettlement
{
  void create(String auctionId,
              String userId,
              AuctionDataPublic.Bid bid,
              Result<Boolean> result);

  void commit(Result<Status> status);

  void rollback(Result<Status> status);

  void status(Result<Status> status);

  void getTransactionState(Result<SettlementTransactionState> result);

  enum Status
  {
    COMMITTED,
    COMMITTING,
    COMMIT_FAILED,
    ROLLING_BACK,
    ROLLED_BACK
  }
}

