package examples.auction;

import static io.baratine.web.Web.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.ramp.jamp.WebJamp;

public class Main
{
  public static void main(String[] args)
  {
    //property("server.file", "classpath:/public");
    property("server.file", "src/main/web");

    //websocket("/user/auction-updates").to(AuctionSocket.class);

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
