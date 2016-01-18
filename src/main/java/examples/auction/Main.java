package examples.auction;

import com.caucho.v5.ramp.jamp.WebJamp;
import io.baratine.web.HttpStatus;
import io.baratine.web.RequestWeb;
import io.baratine.web.ViewWeb;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.baratine.web.Web.*;

public class Main
{
  public static void main(String[] args)
  {
    service(AuctionAdminSessionImpl.class);
    service(AuctionSessionImpl.class);
    service(AuctionManagerImpl.class);
    service(AuctionSettlementManagerImpl.class);
    service(AuditServiceImpl.class);
    service(IdentityManagerImpl.class);
    service(PayPalImpl.class);
    service(UserManagerImpl.class);

    route("/jamp").to(WebJamp.class);

    Level level = Level.FINER;

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

  public static InputStream getIndexHtmlInputStream(String path)
  {
    InputStream in = Main.class.getResourceAsStream(path);

    System.out.println("Main.getIndexHtmlInputStream " + path + "  " + in);

    return in;
  }

  static class InputStreamView implements ViewWeb<InputStream>
  {
    @Override
    public boolean render(RequestWeb requestWeb, InputStream in)
    {
      byte[] buffer = new byte[2048];
      int l;

      try {
        while ((l = in.read(buffer)) > 0) {
          requestWeb.write(buffer, 0, l);
        }

        requestWeb.ok();
      } catch (IOException e) {
        requestWeb.halt(HttpStatus.INTERNAL_SERVER_ERROR);
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      return true;
    }
  }
}
