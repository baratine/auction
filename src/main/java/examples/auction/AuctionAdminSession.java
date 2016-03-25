package examples.auction;

import io.baratine.service.Result;

/**
 * Admin visible channel facade at session://web/auction-session
 */
public interface AuctionAdminSession extends AuctionSession
{
  void getWinner(String auctionId, Result<WebUser> result);

  void getSettlementState(String auctionId,
                          Result<SettlementTransactionState> result);

  void refund(String id, Result<Boolean> result);
}
