package examples.auction;

import io.baratine.service.Result;

public interface AuditService
{
  void auctionCreate(AuctionDataInit initData, Result<Void> ignore);

  void auctionLoad(AuctionDataPublic auction, Result<Void> ignore);

  void auctionSave(AuctionDataPublic auction, Result<Void> ignore);

  void auctionToOpen(AuctionDataPublic auction, Result<Void> ignore);

  void auctionToClose(AuctionDataPublic auction, Result<Void> ignore);

  void auctionBid(AuctionDataPublic auction,
                  AuctionBid bid,
                  Result<Void> ignore);

  void auctionBidAccept(AuctionBid bid, Result<Void> ignore);

  void auctionBidReject(AuctionBid bid, Result<Void> ignore);

  void settlementRequestAccepted(String auctionId, Result<Void> ignore);

  void settlementRequestPersisted(String settlementId,
                                  String auctionId,
                                  Result<Void> ignore);

  void settlementAuctionWillSettle(String settlementId,
                                   AuctionDataPublic auction,
                                   Auction.Bid bid,
                                   Result<Void> ignore);

  void settlementCompletingWithPayment(String settlementId,
                                       String auctionId,
                                       Payment payment,
                                       Result<Void> ignore);

  void payPalReceivePaymentResponse(String settlementId,
                                    AuctionDataPublic auction,
                                    Payment payment,
                                    Result<Void> ignore);

  void payPalSendPaymentRequest(String settlementId,
                                AuctionDataPublic auction,
                                Auction.Bid bid,
                                String userId,
                                Result<Void> ignore);

  void payPalSendRefund(String settlementId,
                        String saleId,
                        Result<Void> ignore);

  void payPalReceiveRefundResponse(String settlementId,
                                   String saleId,
                                   Refund refund,
                                   Result<Void> ignore);
}
