package examples.auction.s1;

import examples.auction.AuctionDataPublic;
import io.baratine.core.Result;

public interface AuctionSettlement
{
  void create(String auctionId,
              String userId,
              AuctionDataPublic.Bid bid,
              Result<Boolean> result);

  void settle(Result<SettlementStatus> status);

  void cancel(Result<SettlementStatus> status);

  void status(Result<SettlementStatus> status);
}
