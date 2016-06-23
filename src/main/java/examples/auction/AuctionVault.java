package examples.auction;

import io.baratine.service.Service;

@Service("/Auction")
public interface AuctionVault extends AuctionAbstractVault<AuctionImpl>
{
}
