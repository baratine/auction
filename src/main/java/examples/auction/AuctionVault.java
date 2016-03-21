package examples.auction;

import java.util.List;

import com.caucho.v5.ramp.vault.Sql;
import io.baratine.service.IdAsset;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Vault;

@Service("/auction")
public interface AuctionVault extends Vault<IdAsset,AuctionImpl>
{
  void create(AuctionDataInit data, Result<String> id);

  @Sql("")
  void findByTitle(String title, Result<Auction> auction);

  @Sql("")
  void findAuctionDataByTitle(String title,
                              Result<List<AuctionData>> auction);

  @Sql("")
  void findIdsByTitle(String title, Result<List<Long>> auction);
}
