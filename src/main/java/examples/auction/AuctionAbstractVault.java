package examples.auction;

import java.util.List;

import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.vault.IdAsset;
import io.baratine.vault.Vault;

@Service("/Auction")
public interface AuctionAbstractVault<X extends Auction>
  extends Vault<IdAsset,X>
{
  void create(AuctionDataInit data, Result<IdAsset> id);

  void findByTitle(String title, Result<Auction> auction);

  void findAuctionDataByTitle(String title,
                              Result<List<AuctionData>> auction);

  void findIdsByTitle(String title, Result<List<IdAsset>> auction);
}
