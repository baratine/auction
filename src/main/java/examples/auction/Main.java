package examples.auction;

import static io.baratine.web.Web.service;
import static io.baratine.web.Web.start;

public class Main
{
  public static void main(String[] args)
  {
    service(AuctionAdminSessionImpl.class);
    service(AuctionSessionImpl.class);
    service(AuctionManagerImpl.class);
    service(AuctionSettlementManagerImpl.class);
    service(AuditServiceImpl.class);
    service(IdentityManagerImpl.class);
    service(PayPalImpl.class);
    service(UserManagerImpl.class);

    start();
  }
}
