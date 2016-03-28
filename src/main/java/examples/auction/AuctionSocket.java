package examples.auction;

import java.io.IOException;

import javax.inject.Inject;

import io.baratine.pipe.BrokerPipe;
import io.baratine.pipe.PipeIn;
import io.baratine.pipe.Pipes;
import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.vault.Id;
import io.baratine.web.ServiceWebSocket;
import io.baratine.web.WebSocket;

@Service("session:")
public class AuctionSocket
  implements ServiceWebSocket<String,AuctionUserSession.WebAuction>,
  PipeIn<AuctionSession.WebAuction>
{
  @Id
  private String _id;

  @Inject
  Services _manager;

  @Inject
  @Service("pipe:///test")
  BrokerPipe<AuctionSession.WebAuction> _pipeBroker;

  private WebSocket<AuctionUserSession.WebAuction> _ws;

  @Override
  public void next(String s, WebSocket<AuctionUserSession.WebAuction> webSocket)
    throws IOException
  {
  }

  @Override
  public void ok()
  {

  }

  @Override
  public void fail(Throwable throwable)
  {

  }

  @Override
  public void next(AuctionSession.WebAuction o)
  {
    _ws.next(o);
  }

  @Override
  public void open(WebSocket<AuctionUserSession.WebAuction> webSocket)
  {
    _ws = webSocket;

    _pipeBroker.subscribe(Pipes.in(this));
  }
}
