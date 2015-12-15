package examples.auction;

import io.baratine.core.Result;
import io.baratine.core.Service;

/**
 * Admin visible channel facade at session://web/auction-session
 */
public interface AuctionAdminSession
{
  void createUser(String userName, String password, Result<Boolean> result);

  void login(String userName, String password, Result<Boolean> result);

  void getUser(Result<UserData> result);

  void getWinner(String auctionId, Result<UserDataPublic> result);

  void getSettlementState(String auctionId, Result<SettlementTransactionState> result);

  void getAuction(String id, Result<AuctionDataPublic> result);

  void search(String query, Result<String[]> result);

  void setListener(@Service ChannelListener listener, Result<Boolean> result);

  void addAuctionListener(String id, Result<Boolean> result);

  void refund(String id, Result<Boolean> result);

  void logout(Result<Boolean> result);
}
