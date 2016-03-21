package examples.auction;

import io.baratine.service.Result;

public interface AuditService
{
  void auctionCreate(AuctionDataInit initData, Result<Void> ignore);

  void auctionLoad(AuctionData auction, Result<Void> ignore);

  void auctionSave(AuctionData auction, Result<Void> ignore);

  void auctionToOpen(AuctionData auction, Result<Void> ignore);

  void auctionToClose(AuctionData auction, Result<Void> ignore);

  void auctionBid(AuctionData auction,
                  AuctionBid bid,
                  Result<Void> ignore);

  void auctionBidAccept(AuctionBid bid, Result<Void> ignore);

  void auctionBidReject(AuctionBid bid, Result<Void> ignore);

  void settlementRequestAccepted(String auctionId, Result<Void> ignore);

  void settlementRequestPersisted(String settlementId,
                                  String auctionId,
                                  Result<Void> ignore);

  void settlementAuctionWillSettle(String settlementId,
                                   AuctionData auction,
                                   Auction.Bid bid,
                                   Result<Void> ignore);

  void settlementCompletingWithPayment(String settlementId,
                                       String auctionId,
                                       Payment payment,
                                       Result<Void> ignore);

  void payPalReceivePaymentResponse(String settlementId,
                                    AuctionData auction,
                                    Payment payment,
                                    Result<Void> ignore);

  void payPalSendPaymentRequest(String settlementId,
                                AuctionData auction,
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
