package examples.auction;

/**
 * Callback for registered channel events.
 */
public interface ChannelListener
{
  void onAuctionUpdate(AuctionData data);

  void onAuctionClose(AuctionData data);
}
