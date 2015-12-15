package examples.auction;

import io.baratine.core.Result;

public interface AuctionSettlementManager
{
  void getAuction(String id, Result<Auction> result);

  void getUser(String id, Result<User> result);

  void getPayPal(Result<PayPal> result);

  void getAuditService(Result<AuditService> result);
}
