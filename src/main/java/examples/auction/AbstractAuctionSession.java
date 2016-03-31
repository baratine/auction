package examples.auction;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.baratine.event.Events;
import io.baratine.pipe.BrokerPipe;
import io.baratine.pipe.Pipe;
import io.baratine.pipe.Pipes;
import io.baratine.service.OnDestroy;
import io.baratine.service.OnInit;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.Services;
import io.baratine.vault.Id;
import io.baratine.web.Body;
import io.baratine.web.Form;
import io.baratine.web.Get;
import io.baratine.web.Post;
import io.baratine.web.Query;

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
  protected AuctionVault _auctions;

  @Inject
  @Service("/User")
  private UserVault _users;

  @Inject
  private Events _Events;

  protected User _user;

  protected String _userId;

  private HashMap<String,AuctionEventsImpl> _listenerMap = new HashMap<>();

  private BrokerPipe<WebAuction> _pipeBroker;

  private Pipe<WebAuction> _auctionUpdates;

  @OnInit
  public void init()
  {
    log.log(Level.FINER, "auction updates pipe service " + _pipeBroker);

    _pipeBroker = _manager.service("pipe:///events/" + _id)
                          .as(BrokerPipe.class);

    _pipeBroker.publish(Pipes.out((x, e) -> _auctionUpdates = x));
  }

  @Post("/createUser")
  public void createUser(@Body AuctionUserSession.UserInitData user,
                         Result<WebUser> result)
  {
    _users.create(user, result.of((id, r) ->
                                    getUserService(id.toString()).get(r.of(u -> WebUser
                                      .of(u)))));
  }

  @Post("/login")
  public void login(@Body Form login, Result<Boolean> result)
  {
    String user = login.getFirst("u");
    String password = login.getFirst("p");

    if (user == null || password == null) {
      result.ok(false);
    }
    else {
      _users.findByName(user,
                        result.of((u, r) -> authenticate(u, password, r)));
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
                        result.of((x, r) -> completeLogin(x, user, r)));
    }
  }

  private void completeLogin(boolean isLoggedIn,
                             User user,
                             Result<Boolean> result)
  {
    if (isLoggedIn) {
      _user = user;

      user.get(result.of(u -> {
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

    _user.get(result.of(u -> WebUser.of(u)));
  }

  public User getUserService(String id)
  {
    return _manager.service(User.class, id);
  }

  public void getAuction(String id,
                         Result<AuctionUserSession.WebAuction> result)
  {
    validateSession();

    if (id == null) {
      throw new IllegalArgumentException();
    }

    getAuctionService(id).get(result.of(a -> WebAuction.of(a)));
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

    _auctions.findAuctionDataByTitle(query, result.of(l -> asWebAuctions(l)));
  }

  private List<AuctionUserSession.WebAuction> asWebAuctions(List<AuctionData> auctions)
  {
    return auctions.stream()
                   .map(a -> WebAuction.of(a))
                   .collect(Collectors.toList());
  }

  @Post("/addAuctionListener")
  public void addAuctionListener(@Body String id, Result<Boolean> result)
  {
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

    _Events.subscriber(id, auctionListener, (c, e) -> {});

    auctionListener.subscribe();

    _listenerMap.put(id, auctionListener);
  }

  public void addEvent(AuctionData event)
  {
    _auctionUpdates.next(WebAuction.of(event));
  }

  protected void validateSession()
  {
    if (_user == null)
      throw new IllegalStateException("not logged in");
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
