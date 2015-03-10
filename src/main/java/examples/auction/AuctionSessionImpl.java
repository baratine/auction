package examples.auction;

import io.baratine.core.Lookup;
import io.baratine.core.OnDestroy;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceManager;
import io.baratine.core.ServiceRef;
import io.baratine.core.SessionService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User visible channel facade at channel:///auction-channel.
 */
@SessionService("session://web/auction-session/{_sessionId}")
public class AuctionSessionImpl implements AuctionSession
{
  private final static Logger log
    = Logger.getLogger(AuctionSessionImpl.class.getName());

  private String _sessionId;

  @Inject
  private ServiceManager _manager;

  @Inject @Lookup("pod://user/user")
  private UserManager _users;

  @Inject @Lookup("pod://user/user")
  private ServiceRef _usersServiceRef;

  @Inject @Lookup("pod://auction/auction")
  private AuctionManager _auctions;

  @Inject @Lookup("pod://auction/auction")
  private ServiceRef _auctionsServiceRef;

  private HashMap<String,AuctionEventsImpl> _listenerMap = new HashMap<>();
  private ChannelListener _listener;

  private User _user;
  private String _userId;

  public void createUser(String userName, String password,
                         final Result<Boolean> result)
  {
    _users.create(userName, password, result.from(id -> true));
  }

  public void login(String userName, String password, Result<Boolean> result)
  {
    _users.find(userName, result.from((id, r) -> loginImpl(id, password, r)));
  }

  private void loginImpl(String userId, String password, Result<Boolean> result)
  {
    User user = _usersServiceRef.lookup("/" + userId).as(User.class);

    user.authenticate(password,
                      result.from(b -> completeLogin(b, userId, user)));
  }

  private boolean completeLogin(boolean isLoggedIn, String userId, User user)
  {
    if (isLoggedIn) {
      _user = user;
      _userId = userId;
    }

    return isLoggedIn;
  }

  @Override
  public void logout(Result<Boolean> result)
  {
    _user = null;
    _userId = null;

    unsubscribe();

    result.complete(true);
  }

  /**
   * returns logged in user
   */
  public void getUser(Result<UserDataPublic> userData)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    _user.getUserData(userData);
  }

  public void createAuction(String title,
                            int bid,
                            Result<String> result)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    _auctions.create(_userId, title, bid,
                     result.from((x, r) ->
                                   afterCreateAuction(x, title, r)));
  }

  private void afterCreateAuction(String id, String title,
                                  Result<String> result)
  {
    Auction auction = _auctionsServiceRef.lookup("/" + id).as(Auction.class);

    auction.open(result.from(b -> id));

    //_lucene.update(id, title, Result.empty());
  }

  public void findAuction(String title,
                          Result<String> result)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    _auctions.find(title, result);
  }

  /**
   * Bid on an auction.
   *
   * @param auctionId the auction to bid on
   * @param bid       the new bid
   * @param result    true for successful auction.
   */
  public void bidAuction(String auctionId,
                         int bid,
                         Result<Boolean> result)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    getAuctionService(auctionId).bid(_userId, bid, result);

  }

  private Auction getAuctionService(String id)
  {
    Auction auction
      = _auctionsServiceRef.lookup('/' + id).as(Auction.class);

    return auction;
  }

  public void getAuction(String id, Result<AuctionDataPublic> result)
  {
    if (id == null) {
      throw new IllegalArgumentException();
    }

    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    getAuctionService(id).getAuctionData(result);
  }

  public void setListener(@Service ChannelListener listener,
                          Result<Boolean> result)
  {
    Objects.requireNonNull(listener);

    log.finer("set auction channel listener: " + listener);

    _listener = listener;

    result.complete(true);
  }

  public void addAuctionListener(String id, Result<Boolean> result)
  {
    Objects.requireNonNull(id);
    try {
      addAuctionListenerImpl(id);

      result.complete(true);
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
    String url = "event://auction/auction/" + id;

    ServiceRef eventRef = _manager.lookup(url);

    AuctionEventsImpl auctionListener = new AuctionEventsImpl(eventRef);

    auctionListener.subscribe();

    _listenerMap.put(id, auctionListener);
  }

  @OnDestroy
  public void onDestroy()
  {
    log.finer("destroy auction channel: " + this);

    unsubscribe();
  }

  private void unsubscribe()
  {
    for (AuctionEventsImpl events : _listenerMap.values()) {
      events.unsubscribe();
    }

    _listenerMap.clear();
  }

  private class AuctionEventsImpl implements AuctionEvents
  {
    private final ServiceRef _eventRef;

    AuctionEventsImpl(ServiceRef eventRef)
    {
      _eventRef = eventRef;
    }

    public void subscribe()
    {
      _eventRef.subscribe(this);
    }

    public void unsubscribe()
    {
      _eventRef.unsubscribe(this);
    }

    @Override
    public void onBid(AuctionDataPublic auctionData)
    {
      log.finer("on bid event for auction: " + auctionData);

      try {
        if (_listener != null)
          _listener.onAuctionUpdate(auctionData);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onClose(AuctionDataPublic auctionData)
    {
      log.finer("on close event for auction: " + auctionData);

      _listener.onAuctionClose(auctionData);
    }
  }
}
