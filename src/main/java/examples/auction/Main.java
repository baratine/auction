package examples.auction;

import io.baratine.web.HttpStatus;
import io.baratine.web.RequestWeb;
import io.baratine.web.WebView;

import java.io.IOException;
import java.io.InputStream;

import static io.baratine.web.Web.*;

public class Main
{
  public static void main(String[] args)
  {
/*
    service(AuctionAdminSessionImpl.class);
    service(AuctionSessionImpl.class).address("public:///auction-session");
    service(AuctionManagerImpl.class);
    service(AuctionSettlementManagerImpl.class);
    service(AuditServiceImpl.class);
    service(IdentityManagerImpl.class);
    service(PayPalImpl.class);
    service(UserManagerImpl.class);
*/

    view(new InputStreamView());

    get("/index.html").to(req -> req.ok(getIndexHtmlInputStream("/index.html")));
    get("/").to(req -> req.ok(getIndexHtmlInputStream("/index.html")));

/*
    get("/baratine-js.js").to(req -> req.ok(getIndexHtmlInputStream(
      "/baratine-js.js")));
*/

    start();
  }

  public static InputStream getIndexHtmlInputStream(String path)
  {
    InputStream in = Main.class.getResourceAsStream(path);

    System.out.println("Main.getIndexHtmlInputStream " + path + "  " + in);

    return in;
  }

  static class InputStreamView implements WebView<InputStream>
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
