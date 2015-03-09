package examples.auction;

import io.baratine.core.Lookup;
import io.baratine.core.OnInit;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceManager;
import io.baratine.core.ServiceRef;
import io.baratine.core.Services;
import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;

import javax.inject.Inject;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Notes: Baratine needs to be told when to treat service as secure
 * and the security provider / authenticator to use (possibly for the whole bar)
 * <p/>
 * The api needs to support reconnecting transparently, e.g. when channel
 * expires it needs to be reestablished using the prescribed login sequence
 */
@Service("pod://auction/auction")
public class AuctionManagerImpl
{
  private final static Logger log
    = Logger.getLogger(AuctionManagerImpl.class.getName());

  ServiceRef _self;
  @Inject @Lookup("bardb:///")
  private DatabaseService _db;



  public AuctionManagerImpl()
  {
  }

  @OnInit
  public void init()
  {
    _self = Services.getCurrentService();

    try {
      _db.exec(
        "create table auction (id varchar primary key, title varchar, value object) with hash '/auction/$id'",
        Result.empty());
    } catch (Exception e) {
      log.log(Level.FINE, e.getMessage(), e);
    }
  }

  @OnLookup
  public Object lookup(String path)
  {
    String id = path.substring(1);

    log.finer("lookup auction: " + id);

    return new AuctionImpl(Services.getCurrentManager(), _db, id);
  }

  public void create(String ownerId,
                     String title,
                     int bid,
                     Result<String> auctionId)
  {
    String id = UUID.randomUUID().toString();

    Auction auction = _self.lookup("/" + id).as(Auction.class);

    auction.create(ownerId, title, bid, auctionId);
  }

  public void find(String title, Result<String> result)
  {
    _self.checkpoint();

    _db.findOne("select id from auction where title=?",
                result.from(c -> c != null ? c.getString(1) : null),
                title);
  }
}
