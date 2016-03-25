package examples.auction;

import java.io.IOException;

import javax.inject.Inject;

import io.baratine.pipe.PipeIn;
import io.baratine.pipe.PipeService;
import io.baratine.pipe.Pipes;
import io.baratine.service.Id;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;
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
  ServiceManager _manager;

  @Inject
  @Service("pipe:///test")
  PipeService<AuctionSession.WebAuction> _pipeService;

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

    _pipeService.subscribe(Pipes.in(this));
  }
}
