package examples.auction;

import io.baratine.core.Result;

public interface AuctionSettlement
{
  void settleAuction(String auctionId, Result<Void> result);

  void processPaymentComplete(String auctionId,
                              String userId,
                              String idempotenceKey,
                              Payment payment,
                              Result<Void> result);
}
