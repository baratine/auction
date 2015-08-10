package examples.auction;

import io.baratine.core.Result;

public interface AuditService
{
  public void audit(AuditEvent event,
                    AuctionDataPublic data,
                    Result<Void> ignore);

  public static enum AuditEvent
  {
    CREATE,
    OPEN,
    BID,
    INDEX,
    CLOSE
  }
}
