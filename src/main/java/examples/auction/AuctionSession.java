package examples.auction;

import io.baratine.core.Result;
import io.baratine.core.Service;

/**
 * User visible channel facade at session://web/auction-session
 */
public interface AuctionSession
{
  void createUser(String userName, String password, Result<Boolean> result);

  void login(String userName, String password, Result<Boolean> result);

  void getUser(Result<UserDataPublic> result);

  void createAuction(String title, int bid, Result<String> result);

  void getAuction(String id, Result<AuctionDataPublic> result);

  void findAuction(String title, Result<String> result);

  void search(String query, Result<String[]> result);

  void bidAuction(String id, int bid, Result<Boolean> result);

  void setListener(@Service ChannelListener listener,
                   Result<Boolean> result);

  void addAuctionListener(String idAuction,
                          Result<Boolean> result);

  void logout(Result<Boolean> result);
}
