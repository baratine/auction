package examples.auction.c1;

import io.baratine.core.Service;

@Service("pod://settlement/settlements")
public class AuctionSettlementServiceManagerImpl
  implements AuctionSettlementServiceManager
{
  @Override
  public void settle(String auctionId)
  {

  }
}
