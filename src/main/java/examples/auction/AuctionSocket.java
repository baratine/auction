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
  implements ServiceWebSocket<String,AuctionSession.WebAuction>
{
  @Inject
  ServiceManager _manager;
  @Id
  private String _id;
  private WebSocket<AuctionSession.WebAuction> _webSocket;

  @Override
  public void next(String s, WebSocket<AuctionSession.WebAuction> webSocket)
    throws IOException
  {
  }

  @Override
  public void open(WebSocket<AuctionSession.WebAuction> webSocket)
  {
    _webSocket = webSocket;

    AuctionSession auctionSession
      = _manager.service("session:///AuctionSessionImpl/" + _id)
                .as(AuctionSession.class);

    AuctionSession.WebAuctionUpdateListener listener
      = _manager.newService(new WebAuctionUpdateListener())
                .as(AuctionSession.WebAuctionUpdateListener.class);

    auctionSession.addAuctionUpdateListener(listener);
  }

  class WebAuctionUpdateListener
    implements AuctionSession.WebAuctionUpdateListener
  {
    @Override
    public void auctionUpdated(AuctionSession.WebAuction auction)
    {
      _webSocket.next(auction);
    }
  }
}
