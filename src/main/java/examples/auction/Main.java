package examples.auction;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.baratine.web.Web.*;

public class Main
{
  public static void main(String[] args)
  {
    if (new File("src/main/resources/web/index.htmlx").exists())
      property("server.file", "src/main/resources/web");
    else
      property("server.file", "classpath:/web");

    include(AuctionAdminSessionImpl.class);
    include(AuctionUserSessionImpl.class);

    include(AuctionSettlementVault.class);
    include(AuditServiceImpl.class);
    include(PayPalImpl.class);

    include(UserVault.class);
    include(AuctionVault.class);

    Level level = Level.FINEST;

    Logger.getLogger("com.caucho").setLevel(level);
    Logger.getLogger("examples").setLevel(level);
    Logger.getLogger("core").setLevel(level);

    try {
      start();
    } catch (Exception e) {
      e.printStackTrace();
    }

    Logger.getLogger("com.caucho").setLevel(level);
    Logger.getLogger("examples").setLevel(level);
    Logger.getLogger("core").setLevel(level);
  }
}
