package examples.auction;

import java.util.List;

import io.baratine.service.Result;
import io.baratine.web.Body;

public interface AuctionUserSessionSync extends AuctionUserSession
{
  WebUser createUser(@Body UserInitData user);

  boolean login(String user, String password);

  WebAuction createAuction(String title, int price);

  boolean bidAuction(WebBid bid);

  WebUser getUser();

  WebAuction getAuction(String id);

  Auction findAuction(String title);

  List<WebAuction> searchAuctions(String query);

  void addAuctionListener(String idAuction);

  boolean logout();
}
