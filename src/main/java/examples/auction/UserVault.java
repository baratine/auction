package examples.auction;

import com.caucho.v5.ramp.vault.Sql;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.vault.IdAsset;
import io.baratine.vault.Vault;

@Service("/User")
public interface UserVault extends Vault<IdAsset,UserImpl>
{
  void create(AuctionUserSession.UserInitData userInitData,
              Result<IdAsset> result);

  void findByName(String name, Result<User> result);
}
