package examples.auction;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
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
 * User visible channel facade at session://web/auction-admin-session.
 */
@Service("session:///auction-admin-session")
public class AuctionAdminSessionImpl implements AuctionAdminSession
{
  private final static Logger log
    = Logger.getLogger(AuctionAdminSessionImpl.class.getName());

  private String _sessionId;

  @Inject
  private ServiceManager _manager;

  @Inject
  @Service("/user")
  private UserVault _users;

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
  @Service("/settlement")
  private ServiceRef _settlementsServiceRef;

  private HashMap<String,AuctionEventsImpl> _listenerMap = new HashMap<>();
  private ChannelListener _listener;

  private User _user;
  private String _userId;

  public void createUser(String userName,
                         String password,
                         final Result<Boolean> result)
  {
    //_users.create(userName, password, true, result.of(id -> true));
  }

  public void validateLogin(String userName,
                            String password,
                            Result<Boolean> result)
  {
/*
    _users.find(userName, result.of((id, r) -> validateLoginImpl(id,
                                                                   password,
                                                                   r)));
*/
  }

  private void validateLoginImpl(String userId,
                                 String password,
                                 Result<Boolean> result)
  {
    User user = _usersServiceRef.lookup("/" + userId).as(User.class);

    user.authenticate(password,
                      true,
                      result.of(b -> completeLogin(b, userId, user)));
  }

  private boolean completeLogin(boolean isLoggedIn, String userId, User user)
  {
    if (isLoggedIn) {
      _user = user;
      _userId = userId;
    }

    return isLoggedIn;
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

  @Override
  public void getWinner(String auctionId, Result<UserDataPublic> result)
  {
    Auction auction = getAuctionService(auctionId);

    auction.get(result.of((a, r) -> {
      getUserService(a.getLastBidder()).get(
        r.of(u -> new UserDataPublic(u)));
    }));
  }

  @Override
  public void getSettlementState(String auctionId,
                                 Result<SettlementTransactionState> result)
  {
    getAuctionSettlementService(auctionId,
                                result.of((s, r) -> s.getTransactionState(r)));
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

  private void getAuctionSettlementService(String auctionId,
                                           Result<AuctionSettlement> result)
  {
    getAuctionService(auctionId).getSettlementId(result.of(sid -> {
      return _settlementsServiceRef.lookup('/' + sid)
                                   .as(AuctionSettlement.class);
    }));
  }

  private Auction getAuctionService(String id)
  {
    Auction auction
      = _auctionsServiceRef.lookup('/' + id).as(Auction.class);

    return auction;
  }

  private User getUserService(long id)
  {
    User user = _usersServiceRef.lookup("/" + id).as(User.class);

    return user;
  }

  @Override
  public void search(String query, Result<List<Long>> result)
  {
    log.info(String.format("search %1$s", query));

    _auctions.findIdsByTitle(query, result);
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

  @Override
  public void refund(String id, Result<Boolean> result)
  {
    Auction auction = getAuctionService(id);

    auction.refund(result);
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
