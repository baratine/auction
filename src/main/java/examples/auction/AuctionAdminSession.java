package examples.auction;

import java.util.List;

import io.baratine.service.IdAsset;
import io.baratine.service.Result;

/**
 * Admin visible channel facade at session://web/auction-session
 */
public interface AuctionAdminSession
{
  void createUser(String userName, String password, Result<Boolean> result);

  void validateLogin(String userName, String password, Result<Boolean> result);

  void getUser(Result<UserData> result);

  void getWinner(String auctionId, Result<UserData> result);

  void getSettlementState(String auctionId,
                          Result<SettlementTransactionState> result);

  void getAuction(String id, Result<AuctionData> result);

  void search(String query, Result<List<IdAsset>> result);

  void addAuctionListener(String id, Result<Boolean> result);

  void refund(String id, Result<Boolean> result);

  void logout(Result<Boolean> result);
}
