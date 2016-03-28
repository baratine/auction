package examples.auction;

import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.vault.IdAsset;
import io.baratine.vault.Vault;

@Service("/AuctionSettlement")
public interface AuctionSettlementVault
  extends Vault<IdAsset,AuctionSettlementImpl>
{
  void create(AuctionData data, Result<IdAsset> result);
}
