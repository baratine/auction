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
                  Bid bid,
                  Result<Void> ignore);

  void auctionBidAccept(Bid bid, Result<Void> ignore);

  void auctionBidReject(Bid bid, Result<Void> ignore);

  void settlementRequestAccepted(String auctionId, Result<Void> ignore);

  void settlementRequestPersisted(String settlementId,
                                  String auctionId,
                                  Result<Void> ignore);

  void settlementAuctionWillSettle(String settlementId,
                                   AuctionDataPublic auction,
                                   AuctionDataPublic.Bid bid,
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
                                AuctionDataPublic.Bid bid,
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
