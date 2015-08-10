package examples.auction;

import io.baratine.core.Result;
import io.baratine.core.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service("pod://audit/audit")
public class AuditServiceImpl implements AuditService
{
  public final static Logger log
    = Logger.getLogger(AuditServiceImpl.class.getName());

  @Override
  public void audit(AuditEvent event,
                    AuctionDataPublic data,
                    Result<Void> ignore)
  {
    String message = String.format("%1$s %2$s", data, event);

    log.log(Level.INFO, message);

    ignore.complete(null);
  }
}
