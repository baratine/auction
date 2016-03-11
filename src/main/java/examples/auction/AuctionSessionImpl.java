package examples.auction;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.baratine.service.Cancel;
import io.baratine.service.Id;
import io.baratine.service.Ids;
import io.baratine.service.OnDestroy;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;
import io.baratine.service.ServiceRef;
import io.baratine.web.Body;
import io.baratine.web.CrossOrigin;
import io.baratine.web.Form;
import io.baratine.web.Get;
import io.baratine.web.Post;
import io.baratine.web.Query;

/**
 * User visible channel facade at session:///auction-session.
 */
@Service("session:")
@CrossOrigin(value = "*", allowCredentials = true)
public class AuctionSessionImpl implements AuctionSession
{
  private final static Logger log
    = Logger.getLogger(AuctionSessionImpl.class.getName());

  @Id
  private String _id;

  @Inject
  private ServiceManager _manager;

  @Inject
  @Service("/user")
  private ServiceRef _usersServiceRef;

  @Inject
  @Service("/auction")
  private AuctionVault _auctions;

  @Inject
  @Service("/auction")
  private ServiceRef _auctionsServiceRef;

  @Inject
  @Service("/user")
  private UserVault _users;

  private HashMap<String,AuctionEventsImpl> _listenerMap = new HashMap<>();
  private ChannelListener _listener;

  private User _user;
  private String _userId;

  //
  private transient Set<AuctionDataPublic> _events = new HashSet<>();
  private transient Result<List<WebAuction>> _result;

  @Post()
  public void createUser(@Body UserInitData user, Result<WebUser> result)
  {
    _users.create(user,
                  result.of(id -> new WebUser(Ids.encode(id), user.getUser())));
  }

  @Post()
  public void login(@Body Form login, Result<Boolean> result)
  {
    String user = login.getFirst("u");
    String password = login.getFirst("p");
    _users.findByName(user, result.of((u, r) -> authenticate(u, password, r)));
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
      log.finer("AuctionSessionImpl.completeLogin: " + this);
      log.finer("AuctionSessionImpl.completeLogin: " + _user);
      log.finer("AuctionSessionImpl.completeLogin: " + user);
      user.get(result.of(u -> {
        _userId = u.getId();
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

  @Post()
  public void createAuction(@Body Form form, Result<WebAuction> result)
  {
    log.finer("AuctionSessionImpl.createAuction: " + this);

    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    String title = form.getFirst("t");
    Integer bid = Integer.parseInt(form.getFirst("b"));

    _auctions.create(new AuctionDataInit(_userId, title, bid),
                     result.of((x, r) -> afterCreateAuction(x, r)));
  }

  private void afterCreateAuction(long id, Result<WebAuction> result)
  {
    String encodedId = Ids.encode(id);

    Auction auction =
      _auctionsServiceRef.lookup('/' + encodedId).as(Auction.class);

    auction.open(result.of((b, r) -> getAuction(encodedId, r)));
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

  private WebAuction asWebAuction(AuctionDataPublic auction)
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
  @Get
  public void searchAuctions(@Query("q") String query,
                             Result<List<WebAuction>> result)
  {
    log.info(String.format("search %1$s", query));

    _auctions.findAuctionDataByTitle(query, result.of(l -> asWebAuctions(l)));
  }

  private List<WebAuction> asWebAuctions(List<AuctionDataPublic> auctions)
  {
    System.out.println("AuctionSessionImpl.asWebAuctions " + auctions);

    ArrayList<WebAuction> result = new ArrayList<>();
    for (AuctionDataPublic auction : auctions) {
      result.add(asWebAuction(auction));
    }

    return result;
  }

  private List<String> encodeIds(List<Long> ids)
  {
    return ids.stream().map(x -> Ids.encode(x)).collect(Collectors.toList());
  }

  /**
   * Bid on an auction.
   *
   * @param bid       the new bid
   * @param result    true for successful auction.
   */
  @Post
  public void bidAuction(@Body WebBid bid, Result<Boolean> result)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    getAuctionService(bid.getAuction())
      .bid(new AuctionBid(_userId, bid.getBid()), result);
  }

  public void setListener(@Service ChannelListener listener,
                          Result<Boolean> result)
  {
    Objects.requireNonNull(listener);

    log.finer("set auction channel listener: " + listener);

    _listener = listener;

    result.ok(true);
  }

  @Post
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

  @Override
  @Get
  public void pollEvents(Result<List<WebAuction>> result)
  {
    log.finer("poll events: " + _events);

    List<WebAuction> auctions = new ArrayList<>();

    if (_events.size() > 0) {
      AuctionDataPublic[] events
        = _events.toArray(new AuctionDataPublic[_events.size()]);

      _events.clear();

      for (AuctionDataPublic event : events) {
        auctions.add(new WebAuction(event.getEncodedId(),
                                    event.getTitle(),
                                    event.getLastBid().getBid(),
                                    event.getState().toString()));
      }

      result.ok(auctions);
    }
    else {
      _result = result;
    }
  }

  public void addEvent(AuctionDataPublic event)
  {
    _events.add(event);

    if (_result != null) {
      Result<List<WebAuction>> result = _result;

      _result = null;

      pollEvents(result);
    }
  }

  private void addAuctionListenerImpl(String id)
  {
    if (_listenerMap.containsKey(id))
      return;

    String url = "event:///auction/" + id;

    log.finer("add auction events listener for auction: " + id);

    ServiceRef queue = _manager.service(url);

    AuctionEventsImpl auctionListener = new AuctionEventsImpl(queue);

    auctionListener.subscribe();

    _listenerMap.put(id, auctionListener);
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
    private final ServiceRef _eventRef;
    private Cancel _cancel;

    AuctionEventsImpl(ServiceRef eventRef)
    {
      _eventRef = eventRef;
    }

    public void subscribe()
    {
      _cancel = _eventRef.subscribe(this);
    }

    public void unsubscribe()
    {
      _cancel.cancel();
    }

    @Override
    public void onBid(AuctionDataPublic auctionData)
    {
      log.finer("on bid event for auction: " + auctionData);

      addEvent(auctionData);
    }

    @Override
    public void onClose(AuctionDataPublic auctionData)
    {
      log.finer("on close event for auction: " + auctionData);

      addEvent(auctionData);
    }

    @Override
    public void onSettled(AuctionDataPublic auctionData)
    {
      addEvent(auctionData);
    }

    @Override
    public void onRolledBack(AuctionDataPublic auctionData)
    {
      addEvent(auctionData);
    }
  }
}
