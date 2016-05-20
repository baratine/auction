package examples.auction;

import io.baratine.vault.IdAsset;

public interface UserVaultSync extends UserVault
{
  IdAsset create(AuctionUserSession.UserInitData userInitData);

  User findByName(String name);
}
