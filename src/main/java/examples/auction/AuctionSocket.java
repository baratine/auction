package examples.auction;

import java.io.IOException;

import javax.inject.Inject;

import io.baratine.service.Id;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;
import io.baratine.web.ServiceWebSocket;
import io.baratine.web.WebSocket;

@Service("session:")
public class AuctionSocket
  implements ServiceWebSocket<String,AuctionUserSession.WebAuction>
{
  @Inject
  ServiceManager _manager;
  @Id
  private String _id;
  private WebSocket<AuctionUserSession.WebAuction> _webSocket;

  @Override
  public void next(String s, WebSocket<AuctionUserSession.WebAuction> webSocket)
    throws IOException
  {
  }

  @Override
  public void open(WebSocket<AuctionUserSession.WebAuction> webSocket)
  {
    _webSocket = webSocket;

    AuctionUserSession auctionSession
      = _manager.service("session:///"
                         + AuctionUserSessionImpl.class.getSimpleName()
                         + "/"
                         + _id)
                .as(AuctionUserSession.class);

    AuctionUserSession.WebAuctionUpdateListener listener
      = _manager.newService(new WebAuctionUpdateListener())
                .as(AuctionUserSession.WebAuctionUpdateListener.class);

    auctionSession.addAuctionUpdateListener(listener);
  }

  class WebAuctionUpdateListener
    implements AuctionUserSession.WebAuctionUpdateListener
  {
    @Override
    public void auctionUpdated(AuctionUserSession.WebAuction auction)
    {
      _webSocket.next(auction);
    }
  }
}
