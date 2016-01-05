package examples.auction;

import io.baratine.service.Service;

public interface AuctionSessionSync extends AuctionSession
{
  boolean createUser(String userName, String password);

  boolean login(String userName, String password);

  boolean logout();

  UserData getUser();

  String createAuction(String title, int bid);

  AuctionDataPublic getAuction(String id);

  String findAuction(String title);

  boolean bidAuction(String id, int bid);

  boolean setListener(@Service ChannelListener listener);

  boolean addAuctionListener(String idAuction);
}
