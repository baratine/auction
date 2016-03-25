package examples.auction;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.baratine.event.EventService;
import io.baratine.service.Api;
import io.baratine.service.Id;
import io.baratine.service.OnDestroy;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;
import io.baratine.service.ServiceRef;
import io.baratine.web.Body;
import io.baratine.web.CrossOrigin;
import io.baratine.web.Form;
import io.baratine.web.Get;
import io.baratine.web.Path;
import io.baratine.web.Post;
import io.baratine.web.Query;

/**
 * User visible channel facade at session:///auction-session.
 */
@Service("session:")
@CrossOrigin(value = "*", allowCredentials = true)
@Api(AuctionSession.class)
@Path("/user")
public class AuctionSessionImpl extends AbstractAuctionSession
  implements AuctionSession
{
  private final static Logger log
    = Logger.getLogger(AuctionSessionImpl.class.getName());

  @Id
  private String _id;

  @Inject
  @Service("/auction")
  private AuctionVault _auctions;

  @Inject
  @Service("/auction")
  private ServiceRef _auctionsServiceRef;

  @Inject
  @Service("/user")
  private UserVault _users;

  @Inject
  private EventService _eventService;

  private HashMap<String,AuctionEventsImpl> _listenerMap = new HashMap<>();

  private User _user;
  private String _userId;

  private WebAuctionUpdateListener _webListener;

  @Post("/createUser")
  public void createUser(@Body UserInitData user, Result<WebUser> result)
  {
    _users.create(user,
                  result.of(id -> new WebUser(id.toString(), user.getUser())));
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
      log.finer("AuctionSessionImpl.completeLogin fail: " + this);
      result.ok(false);
    }
  }

  /**
   * returns logged in user
   */
  public void getUser(Result<UserData> userData)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    _user.get(userData);
  }

  @Post("/createAuction")
  public void createAuction(@Body Form form, Result<WebAuction> result)
  {
    log.finer("AuctionSessionImpl.createAuction: " + this);

    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    String title = form.getFirst("t");
    Integer bid = Integer.parseInt(form.getFirst("b"));

    _auctions.create(new AuctionDataInit(_userId, title, bid),
                     result.of((id, r) -> afterCreateAuction(id.toString(),
                                                             r)));
  }

  private void afterCreateAuction(String auctionId, Result<WebAuction> result)
  {
    Auction auction =
      _auctionsServiceRef.lookup('/' + auctionId).as(Auction.class);

    auction.open(result.of((b, r) -> getAuction(auctionId, r)));
  }

  public void getAuction(String id, Result<WebAuction> result)
  {
    if (id == null) {
      throw new IllegalArgumentException();
    }

    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    getAuctionService(id).get(result.of(a -> asWebAuction(a)));
  }

  private WebAuction asWebAuction(AuctionData auction)
  {
    Auction.Bid bid = auction.getLastBid();
    int price = bid != null ? bid.getBid() : auction.getStartingBid();

    WebAuction webAuction = new WebAuction(auction.getEncodedId(),
                                           auction.getTitle(),
                                           price,
                                           auction.getState().toString());

    return webAuction;
  }

  private Auction getAuctionService(String id)
  {
    Auction auction
      = _auctionsServiceRef.lookup('/' + id).as(Auction.class);

    return auction;
  }

  public void findAuction(String title,
                          Result<Auction> result)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    _auctions.findByTitle(title, result);
  }

  @Override
  @Get("/searchAuctions")
  public void searchAuctions(@Query("q") String query,
                             Result<List<WebAuction>> result)
  {
    log.info(String.format("search %1$s", query));

    _auctions.findAuctionDataByTitle(query, result.of(l -> asWebAuctions(l)));
  }

  private List<WebAuction> asWebAuctions(List<AuctionData> auctions)
  {
    return auctions.stream()
                   .map(a -> asWebAuction(a))
                   .collect(Collectors.toList());
  }

  /**
   * Bid on an auction.
   *
   * @param bid    the new bid
   * @param result true for successful auction.
   */
  @Post("/bidAuction")
  public void bidAuction(@Body WebBid bid, Result<Boolean> result)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    getAuctionService(bid.getAuction())
      .bid(new AuctionBid(_userId, bid.getBid()), result);
  }

  @Override
  public void addAuctionUpdateListener(WebAuctionUpdateListener listener)
  {
    _webListener = listener;
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

    _eventService.subscriber(id, auctionListener, (c, e) -> {});

    auctionListener.subscribe();

    _listenerMap.put(id, auctionListener);
  }

  public void addEvent(AuctionData event)
  {
    if (_webListener != null)
      _webListener.auctionUpdated(asWebAuction(event));
  }

  @Override
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
    log.finer("destroy auction channel: " + this);

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
           + ']';
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
