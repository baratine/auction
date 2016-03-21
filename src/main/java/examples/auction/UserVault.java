package examples.auction;

import com.caucho.v5.ramp.vault.Sql;
import io.baratine.service.IdAsset;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Vault;

@Service("/user")
public interface UserVault extends Vault<IdAsset,UserImpl>
{
  void create(AuctionSession.UserInitData userInitData,
              Result<String> result);

  //@Sql("where __doc.name=?")
  @Sql("")
  void findByName(String name, Result<User> result);
}
