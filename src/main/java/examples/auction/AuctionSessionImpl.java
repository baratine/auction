package examples.auction;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.baratine.service.Cancel;
import io.baratine.service.OnDestroy;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;
import io.baratine.service.ServiceRef;

/**
 * User visible channel facade at session:///auction-session.
 */
@Service("session:///auction-session")
public class AuctionSessionImpl implements AuctionSession
{
  private final static Logger log
    = Logger.getLogger(AuctionSessionImpl.class.getName());

  private String _sessionId;

  @Inject
  private ServiceManager _manager;

  @Inject
  @Service("/user")
  private ServiceRef _usersServiceRef;

  @Inject
  @Service("/auction")
  private AuctionManager _auctions;

  @Inject
  @Service("/auction")
  private ServiceRef _auctionsServiceRef;

  @Inject
  @Service("/user")
  private UserVault _users;

  private HashMap<String,AuctionEventsImpl> _listenerMap = new HashMap<>();
  private ChannelListener _listener;

  private User _user;
  private long _userId = -1;

  public void createUser(String userName, String password,
                         final Result<Boolean> result)
  {
    log.log(Level.FINER,
            String.format("AuctionSessionImpl: create user %1$s", userName));

    _users.create(userName, password, false, result.of(id -> true));
  }

  public void validateLogin(String userName,
                            String password,
                            Result<Boolean> result)
  {
    _users.findByName(userName,
                      result.of((u, r) -> authenticate(u, password, r)));
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
        _userId = u.getId();
        return true;
      }));
    }

    result.ok(false);
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

  public void createAuction(String title,
                            int bid,
                            Result<String> result)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    _auctions.create(new AuctionDataInit(_userId, title, bid),
                     result.of((x, r) -> afterCreateAuction(x, title, r)));
  }

  private void afterCreateAuction(String id, String title,
                                  Result<String> result)
  {
    Auction auction = _auctionsServiceRef.lookup("/" + id).as(Auction.class);

    auction.open(result.of(b -> id));
  }

  public void getAuction(String id, Result<AuctionDataPublic> result)
  {
    if (id == null) {
      throw new IllegalArgumentException();
    }

    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    getAuctionService(id).get(result);
  }

  private Auction getAuctionService(String id)
  {
    Auction auction
      = _auctionsServiceRef.lookup('/' + id).as(Auction.class);

    return auction;
  }

  public void findAuction(String title,
                          Result<String> result)
  {
    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    _auctions.find(title, result);
  }

  @Override
  public void search(String query, Result<String[]> result)
  {
    log.info(String.format("search %1$s", query));

    _auctions.search(query).collect(ArrayList<String>::new,
                                    (l, e) -> l.add(e),
                                    (a, b) -> a.addAll(b))
             .result(result.of(l -> l.toArray(new String[l.size()])));
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

    getAuctionService(auctionId).bid(new Bid(_userId, bid), result);
  }

  public void setListener(@Service ChannelListener listener,
                          Result<Boolean> result)
  {
    Objects.requireNonNull(listener);

    log.finer("set auction channel listener: " + listener);

    _listener = listener;

    result.ok(true);
  }

  public void addAuctionListener(String id, Result<Boolean> result)
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
    String url = "event:///auction/" + id;

    ServiceRef eventRef = _manager.lookup(url);

    AuctionEventsImpl auctionListener = new AuctionEventsImpl(eventRef);

    auctionListener.subscribe();

    _listenerMap.put(id, auctionListener);
  }

  @Override
  public void logout(Result<Boolean> result)
  {
    _user = null;
    _userId = -1;

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
           + _sessionId
           + ", "
           + _userId
           + ']';
  }

  private class AuctionEventsImpl implements AuctionEvents
  {
    private final ServiceRef _eventRef;
    private Cancel _Cancel;

    AuctionEventsImpl(ServiceRef eventRef)
    {
      _eventRef = eventRef;
    }

    public void subscribe()
    {
      _Cancel = _eventRef.subscribe(this);
    }

    public void unsubscribe()
    {
      _Cancel.cancel();
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

      if (_listener != null)
        _listener.onAuctionClose(auctionData);
    }

    @Override
    public void onSettled(AuctionDataPublic auctionData)
    {
      if (_listener != null)
        _listener.onAuctionUpdate(auctionData);
    }

    @Override
    public void onRolledBack(AuctionDataPublic auctionData)
    {
      if (_listener != null)
        _listener.onAuctionUpdate(auctionData);
    }
  }
}
