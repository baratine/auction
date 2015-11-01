package examples.auction;

import io.baratine.core.Result;
import io.baratine.core.Service;

import java.util.logging.Logger;

@Service("pod://audit/audit")
public class AuditServiceImpl implements AuditService
{
  public final static Logger log
    = Logger.getLogger(AuditServiceImpl.class.getName());

  @Override
  public void auctionCreate(AuctionDataInit initData, Result<Void> ignore)
  {
    String message = String.format("auction create %1$s", initData);
    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void auctionLoad(AuctionDataPublic auction, Result<Void> ignore)
  {
    String message = String.format("auction load %1$s", auction);
    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void auctionSave(AuctionDataPublic auction, Result<Void> ignore)
  {
    String message = String.format("auction save %1$s", auction);
    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void auctionToOpen(AuctionDataPublic auction, Result<Void> ignore)
  {
    String message = String.format("auction open %1$s", auction);
    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void auctionToClose(AuctionDataPublic auction, Result<Void> ignore)
  {
    String message = String.format("auction close %1$s", auction);
    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void auctionBid(AuctionDataPublic auction,
                         Bid bid,
                         Result<Void> ignore)
  {
    String message = String.format("auction %1$s bid %2$s", auction, bid);
    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void auctionBidAccept(Bid bid, Result<Void> ignore)
  {
    String message = String.format("bid accepted %1$s", bid);
    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void auctionBidReject(Bid bid, Result<Void> ignore)
  {
    String message = String.format("bid rejected %1$s", bid);
    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void settlementRequestAccepted(String auctionId, Result<Void> ignore)
  {
    String message
      = String.format("accepting settle request for auction %1$s", auctionId);

    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void settlementRequestPersisted(String auctionId,
                                         String idempotenceKey,
                                         Result<Void> ignore)
  {
    String message
      = String.format(
      "settlement request for auction %1$s persisted with idempotence key %2$s",
      auctionId,
      idempotenceKey);

    log.info(message);

    ignore.complete(null);

  }

  @Override
  public void settlementAuctionWillSettle(String idempotenceKey,
                                          AuctionDataPublic auction,
                                          AuctionDataPublic.Bid bid,
                                          Result<Void> ignore)
  {
    String message
      = String.format("%1$s: auction %2$s will settle with bid %3$s",
                      idempotenceKey,
                      auction,
                      bid);

    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void settlementCompletingWithPayment(String idempotenceKey,
                                              String auctionId,
                                              Payment payment,
                                              Result<Void> ignore)
  {
    String message
      = String.format(
      "%1$s: settlement for auction %2$s completing with payment %3$s",
      idempotenceKey,
      auctionId,
      payment);

    log.info(message);

    ignore.complete(null);
  }

  @Override
  public void payPalReceivePaymentResponse(String idempotenceKey,
                                           AuctionDataPublic auction,
                                           Payment payment,
                                           Result<Void> ignore)
  {
    String message
      = String.format(
      "%1$s: pay pal payment response %2$s received for auction %3$s",
      idempotenceKey,
      payment,
      auction);

    log.info(message);

    ignore.complete(null);

  }

  @Override
  public void payPalSendPaymentRequest(String idempotenceKey,
                                       AuctionDataPublic auction,
                                       AuctionDataPublic.Bid bid,
                                       String userId,
                                       Result<Void> ignore)
  {
    String message
      = String.format(
      "%1$s: pay pal send payment request for auction %2$s, bid %3$s, user %4$s",
      idempotenceKey,
      auction,
      bid,
      userId);

    log.info(message);

    ignore.complete(null);
  }
}
