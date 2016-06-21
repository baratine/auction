package examples.auction;

import io.baratine.service.Ensure;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.vault.IdAsset;
import io.baratine.vault.Vault;

@Service("/AuctionSettlement")
public interface AuctionSettlementVault
  extends Vault<IdAsset,AuctionSettlementImpl>
{
  void create(AuctionData data, Result<IdAsset> result);

  @Ensure
  default void settle(String id,
                      Auction.Bid bid,
                      Result<AuctionSettlement.Status> result)
  {
    Services.current().service(AuctionSettlementImpl.class, id)
            .settle(bid, result.of());
  }

  @Ensure
  default void refund (String id, Result<AuctionSettlement.Status> result) {
    Services.current().service(AuctionSettlementImpl.class, id)
            .refund(result.of());

  }
}

