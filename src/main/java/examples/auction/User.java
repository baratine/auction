package examples.auction;

import io.baratine.service.Modify;
import io.baratine.service.Service;
import io.baratine.vault.IdAsset;
import io.baratine.service.Result;

@Service
public interface User
{
  @Modify
  void create(AuctionUserSessionImpl.UserInitData user,
              Result<IdAsset> userId);

  void authenticate(String password,
                    boolean isAdmin,
                    Result<Boolean> result);

  void get(Result<UserData> user);

  void getCreditCard(Result<CreditCard> creditCard);

  void addWonAuction(String auction, Result<Boolean> result);

  void removeWonAuction(String auction, Result<Boolean> result);
}
