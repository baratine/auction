package examples.auction;

import io.baratine.service.Api;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.vault.Asset;

@Service
@Asset
@Api
public interface AuctionSettlement
{
  void settle(Auction.Bid bid, Result<Status> result);

  void settleResume(Result<Status> result);

  void refund(Result<Status> status);

  void settleStatus(Result<Status> status);

  void refundStatus(Result<Status> status);

  void getTransactionState(Result<SettlementTransactionState> result);

  enum Status
  {
    NONE,
    SETTLING,
    SETTLED,
    SETTLE_FAILED,
    ROLLING_BACK,
    ROLLED_BACK,
    ROLLBACK_FAILED
  }
}

