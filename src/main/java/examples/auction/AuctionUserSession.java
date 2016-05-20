package examples.auction;

import io.baratine.service.Result;

/**
 * User visible channel facade at session://web/auction-session
 */
public interface AuctionUserSession extends AuctionSession
{
  void createAuction(String title, int price, Result<WebAuction> result);

  void bidAuction(WebBid bid, Result<Boolean> result);

  class WebBid
  {
    private String auction;
    private int bid;

    public WebBid(String auction, int bid)
    {
      this.auction = auction;
      this.bid = bid;
    }

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
