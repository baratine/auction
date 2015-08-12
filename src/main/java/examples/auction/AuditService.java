package examples.auction;

import io.baratine.core.Result;

public interface AuditService
{
  void auctionCreate(AuctionDataInit initData, Result<Void> ignore);

  void auctionLoad(AuctionDataPublic auction, Result<Void> ignore);

  void auctionSave(AuctionDataPublic auction, Result<Void> ignore);

  void auctionToOpen(AuctionDataPublic auction, Result<Void> ignore);

  void auctionToClose(AuctionDataPublic auction, Result<Void> ignore);

  void auctionBid(AuctionDataPublic auction,
                  Bid bid,
                  Result<Void> ignore);

  void auctionBidAccept(Bid bid, Result<Void> ignore);

  void auctionBidReject(Bid bid, Result<Void> ignore);
}
