package examples.auction;

import com.caucho.v5.data.Sql;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Vault;

@Service("/user")
public interface UserVault extends Vault<Long,UserImpl>
{
  void create(AuctionSession.UserInitData userInitData,
              Result<Long> result);

  //@Sql("where __doc.name=?")
  @Sql("")
  void findByName(String name, Result<User> result);
}
