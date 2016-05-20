package examples.auction;

import io.baratine.vault.IdAsset;

public interface UserSync extends User
{
  IdAsset create(AuctionUserSessionImpl.UserInitData user);

  boolean authenticate(String password,
                       boolean isAdmin);

  UserData get();

  CreditCard getCreditCard();

  boolean addWonAuction(String auction);

  boolean removeWonAuction(String auction);
}
