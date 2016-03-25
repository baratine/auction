package examples.auction;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import io.baratine.event.EventService;
import io.baratine.service.Cancel;
import io.baratine.service.IdAsset;
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

  private User _user;
  private String _userId;

  @Inject
  private EventService _eventService;

  private AuctionSession.WebAuctionUpdateListener _listener;

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
  public void getWinner(String auctionId, Result<UserData> result)
  {
    Auction auction = getAuctionService(auctionId);

    auction.get(result.of((a, r) -> {
      getUserService(a.getLastBidder()).get(r.of());
    }));
  }

  private Auction getAuctionService(String id)
  {
    Auction auction
      = _auctionsServiceRef.lookup('/' + id).as(Auction.class);

    return auction;
  }

  private User getUserService(String id)
  {
    User user = _usersServiceRef.lookup("/" + id).as(User.class);

    return user;
  }

  @Override
  public void getSettlementState(String auctionId,
                                 Result<SettlementTransactionState> result)
  {
    getAuctionSettlementService(auctionId,
                                result.of((s, r) -> s.getTransactionState(r)));
  }

  private void getAuctionSettlementService(String auctionId,
                                           Result<AuctionSettlement> result)
  {
    getAuctionService(auctionId).getSettlementId(result.of(sid -> {
      return _settlementsServiceRef.lookup('/' + sid)
                                   .as(AuctionSettlement.class);
    }));
  }

  public void getAuction(String id, Result<AuctionData> result)
  {
    if (id == null) {
      throw new IllegalArgumentException();
    }

    if (_user == null) {
      throw new IllegalStateException("No user is logged in");
    }

    getAuctionService(id).get(result);
  }

  @Override
  public void search(String query, Result<List<IdAsset>> result)
  {
    log.info(String.format("search %1$s", query));

    _auctions.findIdsByTitle(query, result);
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
    if (_listenerMap.containsKey(id))
      return;

    log.finer("add auction events listener for auction: " + id);

    AuctionEventsImpl auctionListener = new AuctionEventsImpl(null);

    _eventService.subscriber(id, auctionListener, (c, e) -> {});

    auctionListener.subscribe();

    _listenerMap.put(id, auctionListener);
  }

  @Override
  public void refund(String id, Result<Boolean> result)
  {
    Auction auction = getAuctionService(id);

    auction.refund(result);
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

  private AuctionSession.WebAuction asWebAuction(AuctionData auction)
  {
    Auction.Bid bid = auction.getLastBid();
    int price = bid != null ? bid.getBid() : auction.getStartingBid();

    AuctionSession.WebAuction
      webAuction = new AuctionSession.WebAuction(auction.getEncodedId(),
                                                 auction.getTitle(),
                                                 price,
                                                 auction.getState().toString());

    return webAuction;
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
    public void onBid(AuctionData auctionData)
    {
      log.finer("on bid event for auction: " + auctionData);

      try {
        if (_listener != null)
          _listener.auctionUpdated(asWebAuction(auctionData));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onClose(AuctionData auctionData)
    {
      log.finer("on close event for auction: " + auctionData);

      if (_listener != null)
        _listener.auctionUpdated(asWebAuction(auctionData));
    }

    @Override
    public void onSettled(AuctionData auctionData)
    {
      if (_listener != null)
        _listener.auctionUpdated(asWebAuction(auctionData));
    }

    @Override
    public void onRolledBack(AuctionData auctionData)
    {
      if (_listener != null)
        _listener.auctionUpdated(asWebAuction(auctionData));
    }
  }
}
