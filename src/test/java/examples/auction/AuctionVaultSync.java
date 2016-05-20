package examples.auction;

import java.util.List;

import io.baratine.vault.IdAsset;

public interface AuctionVaultSync extends AuctionVault
{
  IdAsset create(AuctionDataInit data);

  Auction findByTitle(String title);

  List<AuctionData> findAuctionDataByTitle(String title);

  List<IdAsset> findIdsByTitle(String title);
}
