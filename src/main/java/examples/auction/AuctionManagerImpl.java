package examples.auction;

import com.caucho.lucene.LuceneEntry;
import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.OnInit;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.ResultStream;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.core.Services;
import io.baratine.db.DatabaseService;
import io.baratine.stream.StreamBuilder;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@Service("pod://auction/auction")
@Journal
public class AuctionManagerImpl implements AuctionManager
{
  private final static Logger log
    = Logger.getLogger(AuctionManagerImpl.class.getName());

  ServiceRef _self;

  @Inject @Lookup("bardb:///")
  private DatabaseService _db;

  @Inject @Lookup("/identity-manager")
  private IdentityManager _identityManager;

  @Inject @Lookup("pod://lucene/service")
  private com.caucho.lucene.LuceneFacade _lucene;

  public AuctionManagerImpl()
  {
  }

  @OnInit
  public void init(Result<Boolean> result)
  {
    _self = Services.getCurrentService();

    try {
      _db.exec(
        "create table auction (id varchar primary key, title varchar, value object) with hash '/auction/$id'",
        result.from(o -> o != null));
    } catch (Exception e) {
      log.log(Level.FINE, e.getMessage(), e);
      //assume that exception is due to existing table and complete with true
      result.complete(true);
    }
  }

  @OnLookup
  public Object lookup(String path)
  {
    String id = path.substring(1);

    log.finer("lookup auction: " + id);

    return new AuctionImpl(Services.getCurrentManager(), _db, id);
  }

  @Override
  public void create(String ownerId,
                     String title,
                     int bid,
                     Result<String> auctionId)
  {
    _identityManager.nextId(auctionId.from((id, r)
                                             -> createWithId(id,
                                                             ownerId,
                                                             title,
                                                             bid,
                                                             r)));
  }

  private void createWithId(String id,
                            String ownerId,
                            String title,
                            int bid,
                            Result<String> auctionId)
  {
    Auction auction = _self.lookup("/" + id).as(Auction.class);

    auction.create(ownerId, title, bid, auctionId.from((x, r) -> {
      index(x, title, r);
    }));
  }

  private void index(String id, String title, Result<String> auctionId)
  {
    auctionId.complete(id);

    _lucene.indexText("auction", id, title, Result.ignore());
  }

  public void find(String title, Result<String> result)
  {
    _self.save();

    _db.findOne("select id from auction where title=?",
                result.from(c -> c != null ? c.getString(1) : null),
                title);
  }

  @Override
  public StreamBuilder<String> search(String query)
  {
    throw new AbstractMethodError();
  }

  public void search(String query, ResultStream<String> results)
  {
    _lucene.search("auction", query, 255, results.from((x, r) -> {
      searchImp(x, r);
    }));
  }

  public void searchImp(List<LuceneEntry> entries, ResultStream<String> stream)
  {
    for (LuceneEntry l : entries) {
      stream.accept(l.getExternalId());
    }

    stream.complete();
  }
}
