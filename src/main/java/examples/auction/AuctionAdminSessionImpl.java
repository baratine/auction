package examples.auction;

import java.util.logging.Logger;

import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.web.Body;
import io.baratine.web.CrossOrigin;
import io.baratine.web.Get;
import io.baratine.web.Path;
import io.baratine.web.Post;
import io.baratine.web.Query;

/**
 * User visible channel facade at session://web/auction-admin-session.
 */
@Service("session:")
@CrossOrigin(value = "*", allowCredentials = true)
//@Api(AuctionAdminSession.class)
@Path("/admin")
public class AuctionAdminSessionImpl extends AbstractAuctionSession
  implements AuctionAdminSession
{
  private final static Logger log
    = Logger.getLogger(AuctionAdminSessionImpl.class.getName());

  @Override
  @Get("/winner")
  public void getWinner(@Query("id") String auctionId, Result<WebUser> result)
  {
    validateSession();

    Auction auction = getAuctionService(auctionId);

    auction.get(result.of((a, r) -> {
      getUserService(a.getLastBidder())
        .get(r.of(u -> WebUser.of(u)));
    }));
  }

  @Override
  @Get("/settlement")
  public void getSettlementState(@Query("id") String auctionId,
                                 Result<SettlementTransactionState> result)
  {
    validateSession();

    getAuctionSettlementService(auctionId,
                                result.of((s, r) -> s.getTransactionState(r)));
  }

  private void getAuctionSettlementService(String auctionId,
                                           Result<AuctionSettlement> result)
  {
    getAuctionService(auctionId).getSettlementId(result.of(sid -> {
      return _manager.service(AuctionSettlement.class, sid);
    }));
  }

  @Override
  @Post("/refund")
  public void refund(@Body String id, Result<Boolean> result)
  {
    validateSession();

    Auction auction = getAuctionService(id);

    auction.refund(result);
  }
}
