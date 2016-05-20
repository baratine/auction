package examples.auction;

import java.util.List;

import io.baratine.web.Body;
import io.baratine.web.Form;

public interface AuctionSessionSync extends AuctionSession
{
  WebUser createUser(@Body UserInitData user);

  boolean login(@Body Form login);

  WebUser getUser();

  WebAuction getAuction(String id);

  Auction findAuction(String title);

  List<WebAuction> searchAuctions(String query);

  void addAuctionListener(String idAuction);

  boolean logout();
}
