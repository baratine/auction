package examples.auction;

import java.util.List;

import io.baratine.service.IdAsset;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Vault;

@Service("/Auction")
public interface AuctionVault extends Vault<IdAsset,AuctionImpl>
{
  void create(AuctionDataInit data, Result<IdAsset> id);

  void findByTitle(String title, Result<Auction> auction);

  void findAuctionDataByTitle(String title,
                              Result<List<AuctionData>> auction);

  void findIdsByTitle(String title, Result<List<IdAsset>> auction);
}
