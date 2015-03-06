package examples.auction;

import io.baratine.core.Service;

public interface SyncAuctionSession extends AuctionSession
{
  boolean createUser(String userName, String password);

  boolean login(String userName, String password);

  UserDataPublic getUser();

  String createAuction(String title, int bid);

  AuctionDataPublic getAuction(String id);

  String findAuction(String title);

  boolean bidAuction(String id, int bid);

  boolean setListener(@Service ChannelListener listener);

  boolean addAuctionListener(String idAuction);
}
