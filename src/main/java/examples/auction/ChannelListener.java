package examples.auction;

/**
 * Callback for registered channel events.
 */
public interface ChannelListener
{
  void onAuctionUpdate(AuctionDataPublic data);

  void onAuctionClose(AuctionDataPublic data);
}
