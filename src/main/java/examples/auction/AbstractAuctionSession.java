package examples.auction;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.baratine.event.Events;
import io.baratine.service.OnDestroy;
import io.baratine.service.OnInit;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.vault.Id;
import io.baratine.web.Body;
import io.baratine.web.Get;
import io.baratine.web.Post;
import io.baratine.web.Query;
import io.baratine.web.RequestWeb;
import io.baratine.web.ServiceWebSocket;
import io.baratine.web.WebSocket;
import io.baratine.web.WebSocketPath;

public class AbstractAuctionSession implements AuctionSession
{
  private final static Logger log
    = Logger.getLogger(AbstractAuctionSession.class.getName());

  @Id
  protected String _id;

  @Inject
  protected Services _manager;

  @Inject
  @Service("/Auction")
  protected AuctionAbstractVault<Auction> _auctions;
  protected User _user;
  protected String _userId;
  @Inject
  @Service("/User")
  private UserAbstractVault<User> _users;
  @Inject
  @Service("event:")
  private Events _events;
  private HashMap<String,AuctionEventsImpl> _listenerMap = new HashMap<>();
  private WebAuctionUpdates _updates;

  @OnInit
  public void init()
  {
  }

  @Post("/createUser")
  public void createUser(@Body AuctionUserSession.UserInitData user,
                         Result<WebUser> result)
  {
    _users.create(user, result.then((id, r) ->
                                      getUserService(id.toString()).get(r.then(u -> WebUser
                                        .of(u)))));
  }

  public User getUserService(String id)
  {
    return _manager.service(User.class, id);
  }

  @Post("/login")
  public void login(@Body("u") String user,
                    @Body("p") String password,
                    Result<Boolean> result)
  {
    if (user == null || password == null) {
      result.ok(false);
    }
    else {
      _users.findByName(user,
                        result.then((u, r) -> authenticate(u,
                                                           password,
                                                           r)));
    }
  }

  private void authenticate(User user, String password, Result<Boolean> result)
  {
    if (user == null) {
      result.ok(false);
    }
    else {
      user.authenticate(password,
                        false,
                        result.then((x, r) -> completeLogin(x, user, r)));
    }
  }

  private void completeLogin(boolean isLoggedIn,
                             User user,
                             Result<Boolean> result)
  {
    if (isLoggedIn) {
      _user = user;

      user.get(result.then(u -> {
        _userId = u.getEncodedId();
        return true;
      }));
    }
    else {
      result.ok(false);
    }
  }

  /**
   * returns logged in user
   */
  public void getUser(Result<WebUser> result)
  {
    validateSession();

    _user.get(result.then(u -> WebUser.of(u)));
  }

  protected void validateSession()
  {
    if (_user == null)
      throw new IllegalStateException("not logged in");
  }

  public void getAuction(String id,
                         Result<AuctionUserSession.WebAuction> result)
  {
    validateSession();

    if (id == null) {
      throw new IllegalArgumentException();
    }

    getAuctionService(id).get(result.then(a -> WebAuction.of(a)));
  }

  protected Auction getAuctionService(String id)
  {
    Auction auction = _manager.service(Auction.class, id);

    return auction;
  }

  public void findAuction(String title,
                          Result<Auction> result)
  {
    validateSession();

    _auctions.findByTitle(title, result);
  }

  @Get("/searchAuctions")
  public void searchAuctions(@Query("q") String query,
                             Result<List<AuctionUserSession.WebAuction>> result)
  {
    validateSession();

    AbstractAuctionSession.log.info(String.format("search %1$s", query));

    _auctions.findAuctionDataByTitle(query, result.then(l -> asWebAuctions(l)));
  }

  private List<AuctionUserSession.WebAuction> asWebAuctions(List<AuctionData> auctions)
  {
    return auctions.stream()
                   .map(a -> WebAuction.of(a))
                   .collect(Collectors.toList());
  }

  @WebSocketPath("/auction-updates")
  public void updates(RequestWeb request)
  {
    _updates = new WebAuctionUpdates();

    request.upgrade(_updates);
  }

  @Post("/addAuctionListener")
  public void addAuctionListener(@Body String id, Result<Boolean> result)
  {
    validateSession();

    Objects.requireNonNull(id);
    try {
      addAuctionListenerImpl(id);

      result.ok(true);
    } catch (Throwable t) {
      log.log(Level.WARNING, t.getMessage(), t);

      if (t instanceof RuntimeException)
        throw (RuntimeException) t;
      else
        throw new RuntimeException(t);
    }
  }

  private void addAuctionListenerImpl(String id)
  {
    if (_listenerMap.containsKey(id))
      return;

    log.finer("add auction events listener for auction: " + id);

    AuctionEventsImpl auctionListener = new AuctionEventsImpl();

    _events.subscriber(id, auctionListener, (c, e) -> {
    });

    auctionListener.subscribe();

    _listenerMap.put(id, auctionListener);
  }

  public void addEvent(AuctionData event)
  {
    if (_updates != null)
      _updates.next(WebAuction.of(event));
  }

  public void logout(Result<Boolean> result)
  {
    _user = null;
    _userId = null;

    unsubscribe();

    result.ok(true);
  }

  private void unsubscribe()
  {
    for (AuctionEventsImpl events : _listenerMap.values()) {
      events.unsubscribe();
    }

    _listenerMap.clear();
  }

  @OnDestroy
  public void destroy()
  {
    AbstractAuctionSession.log.finer("destroy auction channel: " + this);

    unsubscribe();
  }

  @Override
  public String toString()
  {
    return this.getClass().getSimpleName()
           + '['
           + _id
           + ", "
           + _userId
           + "]@" + System.identityHashCode(this);
  }

  class WebAuctionUpdates implements ServiceWebSocket<WebAuction,WebAuction>
  {
    private WebSocket<WebAuction> _updatesSocket;

    @Override
    public void open(WebSocket<WebAuction> webSocket)
    {
      _updatesSocket = webSocket;
    }

    @Override
    public void next(WebAuction auction,
                     WebSocket<WebAuction> webSocket)
      throws IOException
    {

    }

    public void next(WebAuction auction)
    {
      _updatesSocket.next(auction);
    }
  }

  private class AuctionEventsImpl implements AuctionEvents
  {
    AuctionEventsImpl()
    {
    }

    public void subscribe()
    {

    }

    public void unsubscribe()
    {

    }

    @Override
    public void onBid(AuctionData auctionData)
    {
      log.finer("on bid event for auction: " + auctionData);

      addEvent(auctionData);
    }

    @Override
    public void onClose(AuctionData auctionData)
    {
      log.finer("on close event for auction: " + auctionData);

      addEvent(auctionData);
    }

    @Override
    public void onSettled(AuctionData auctionData)
    {
      addEvent(auctionData);
    }

    @Override
    public void onRolledBack(AuctionData auctionData)
    {
      addEvent(auctionData);
    }
  }
}
