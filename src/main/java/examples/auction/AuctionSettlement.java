package examples.auction;

import io.baratine.core.Result;

public interface AuctionSettlement
{
  void settle(String auctionId,
              String userId,
              AuctionDataPublic.Bid bid,
              Result<Status> result);

  void settleResume(Result<Status> result);

  void rollback(Result<Status> status);

  void settleStatus(Result<Status> status);

  void rollbackStatus(Result<Status> status);

  void getTransactionState(Result<SettlementTransactionState> result);

  enum Status
  {
    NONE,
    SETTLING,
    SETTLED,
    COMMIT_FAILED,
    ROLLING_BACK,
    ROLLED_BACK,
    ROLLBACK_FAILED
  }
}

