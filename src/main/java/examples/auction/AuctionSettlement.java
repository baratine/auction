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

  void commitStatus(Result<Status> status);

  void rollbackStatus(Result<Status> status);

  void getTransactionState(Result<SettlementTransactionState> result);

  enum Status
  {
    NONE,
    COMMITTING,
    COMMITTED,
    COMMIT_FAILED,
    ROLLING_BACK,
    ROLLED_BACK,
    ROLLBACK_FAILED
  }
}

