package examples.auction;

import java.util.logging.Logger;

import javax.inject.Inject;

import io.baratine.service.Api;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceRef;
import io.baratine.web.CrossOrigin;
import io.baratine.web.Path;

/**
 * User visible channel facade at session://web/auction-admin-session.
 */
@Service("session:")
@CrossOrigin(value = "*", allowCredentials = true)
@Api(AuctionAdminSession.class)
@Path("/admin")
public class AuctionAdminSessionImpl extends AbstractAuctionSession
  implements AuctionAdminSession
{
  private final static Logger log
    = Logger.getLogger(AuctionAdminSessionImpl.class.getName());

  @Inject
  @Service("/settlement")
  private ServiceRef _settlements;

  @Override
  public void getWinner(String auctionId, Result<WebUser> result)
  {
    Auction auction = getAuctionService(auctionId);

    auction.get(result.of((a, r) -> {
      getUserService(a.getLastBidder())
        .get(r.of(u -> new WebUser(u.getEncodedId(), u.getName())));
    }));
  }

  @Override
  public void getSettlementState(String auctionId,
                                 Result<SettlementTransactionState> result)
  {
    getAuctionSettlementService(auctionId,
                                result.of((s, r) -> s.getTransactionState(r)));
  }

  private void getAuctionSettlementService(String auctionId,
                                           Result<AuctionSettlement> result)
  {
    getAuctionService(auctionId).getSettlementId(result.of(sid -> {
      return _settlements.lookup('/' + sid)
                         .as(AuctionSettlement.class);
    }));
  }

  @Override
  public void refund(String id, Result<Boolean> result)
  {
    Auction auction = getAuctionService(id);

    auction.refund(result);
  }
}
