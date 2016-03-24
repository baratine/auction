package examples.auction;

import io.baratine.service.IdAsset;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Vault;

@Service("/user")
public interface UserVault extends Vault<IdAsset,UserImpl>
{
  void create(AuctionSession.UserInitData userInitData,
              Result<IdAsset> result);

  void findByName(String name, Result<User> result);
}
