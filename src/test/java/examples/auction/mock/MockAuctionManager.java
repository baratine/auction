package examples.auction.mock;

import examples.auction.Auction;
import examples.auction.AuctionDataInit;
import examples.auction.AuctionDataPublic;
import examples.auction.AuctionManagerImpl;
import examples.auction.Bid;
import io.baratine.core.Journal;
import io.baratine.core.Result;
import io.baratine.core.Service;

@Service("pod://auction/auction")
@Journal()
public class MockAuctionManager extends AuctionManagerImpl
{
  @Override
  public Object lookup(String path)
  {
    return new AuctionWrapper((Auction) super.lookup(path));
  }
}

class AuctionWrapper implements Auction
{
  private Auction _auction;

  public AuctionWrapper(Auction auction)
  {
    _auction = auction;
  }

  @Override
  public void create(AuctionDataInit initData,
                     Result<String> result)
  {
    _auction.create(initData, result);
  }

  @Override
  public void open(Result<Boolean> result)
  {
    _auction.open(result);
  }

  @Override
  public void bid(Bid bid,
                  Result<Boolean> result) throws IllegalStateException
  {
    _auction.bid(bid, result);
  }

  @Override
  public void setAuctionWinner(String user,
                               Result<Boolean> result)
  {
    result.complete(false);
    //_auction.setAuctionWinner(user, result);
  }

  @Override
  public void clearAuctionWinner(String user,
                                 Result<Boolean> result)
  {
    _auction.clearAuctionWinner(user, result);
  }

  @Override
  public void setSettled(Result<Boolean> result)
  {
    _auction.setSettled(result);
  }

  @Override
  public void setRolledBack(Result<Boolean> result)
  {
    _auction.setRolledBack(result);
  }

  @Override
  public void get(Result<AuctionDataPublic> result)
  {
    _auction.get(result);
  }

  @Override
  public void close(Result<Boolean> result)
  {
    _auction.close(result);
  }

  @Override
  public void refund(Result<Boolean> result)
  {
    _auction.refund(result);
  }

  @Override
  public void getSettlementId(Result<String> result)
  {
    _auction.getSettlementId(result);
  }
}
