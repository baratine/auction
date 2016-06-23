package examples.auction;

import java.util.List;

import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.vault.IdAsset;

@Service("/Auction")
public interface AuctionVault extends AuctionAbstractVault<AuctionImpl>
{
}
