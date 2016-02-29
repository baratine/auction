package examples.auction;

import java.util.List;

import com.caucho.v5.data.Sql;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Vault;

@Service("/auction")
public interface AuctionVault extends Vault<Long,AuctionImpl>
{
  void create(AuctionDataInit data, Result<Long> id);

  @Sql("")
  void findByTitle(String title, Result<Auction> auction);

  @Sql("")
  void findIdsByTitle(String title, Result<List<Long>> auction);
}
