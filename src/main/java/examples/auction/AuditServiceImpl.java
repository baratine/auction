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
  }

  @Override
  public void auctionLoad(AuctionDataPublic auction, Result<Void> ignore)
  {
    String message = String.format("auction load %1$s", auction);
    log.info(message);

  }

  @Override
  public void auctionSave(AuctionDataPublic auction, Result<Void> ignore)
  {
    String message = String.format("auction save %1$s", auction);
    log.info(message);

  }

  @Override
  public void auctionToOpen(AuctionDataPublic auction, Result<Void> ignore)
  {
    String message = String.format("auction open %1$s", auction);
    log.info(message);

  }

  @Override
  public void auctionToClose(AuctionDataPublic auction, Result<Void> ignore)
  {
    String message = String.format("auction close %1$s", auction);
    log.info(message);
  }

  @Override
  public void auctionBid(AuctionDataPublic auction,
                         Bid bid,
                         Result<Void> ignore)
  {
    String message = String.format("auction %1$s bid %2$s", auction, bid);
    log.info(message);
  }

  @Override
  public void auctionBidAccept(Bid bid, Result<Void> ignore)
  {
    String message = String.format("bid accepted %1$s", bid);
    log.info(message);
  }

  @Override
  public void auctionBidReject(Bid bid, Result<Void> ignore)
  {
    String message = String.format("bid rejected %1$s", bid);
    log.info(message);
  }
}
