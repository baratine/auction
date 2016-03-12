package examples.auction;

import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Vault;

@Service("/settlement")
public interface AuctionSettlementVault
  extends Vault<Long,AuctionSettlementImpl>
{
  public abstract void create(AuctionDataPublic data, Result<String> result);
}
