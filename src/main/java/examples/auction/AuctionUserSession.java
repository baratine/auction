package examples.auction;

import io.baratine.service.Result;
import io.baratine.web.Form;

/**
 * User visible channel facade at session://web/auction-session
 */
public interface AuctionUserSession extends AuctionSession
{
  void createAuction(Form form, Result<WebAuction> result);

  void bidAuction(WebBid bid, Result<Boolean> result);

  class WebBid
  {
    private String auction;
    private int bid;

    public String getAuction()
    {
      return auction;
    }

    public int getBid()
    {
      return bid;
    }
  }

}
