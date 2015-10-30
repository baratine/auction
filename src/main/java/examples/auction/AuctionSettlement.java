package examples.auction;

import io.baratine.core.Result;

public interface AuctionSettlement
{
  void auctionClosed(String auctionId, Result<Void> result);

  void processPaymentComplete(String auctionId,
                              String userId,
                              String idempotencyKey,
                              Payment payment,
                              Result<Void> result);
}
