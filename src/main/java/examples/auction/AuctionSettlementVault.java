package examples.auction;

import io.baratine.service.IdAsset;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Vault;

@Service("/settlement")
public interface AuctionSettlementVault
  extends Vault<IdAsset,AuctionSettlementImpl>
{
  void create(AuctionData data, Result<String> result);
}
