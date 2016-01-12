package examples.auction;

import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.service.Journal;
import io.baratine.service.OnInit;
import io.baratine.service.OnLookup;
import io.baratine.service.Result;
import io.baratine.service.ResultStream;
import io.baratine.service.Service;
import io.baratine.service.ServiceManager;
import io.baratine.service.ServiceRef;
import io.baratine.stream.ResultStreamBuilder;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@Service("/auction")
@Journal()
public class AuctionManagerImpl implements AuctionManager
{
  private final static Logger log
    = Logger.getLogger(AuctionManagerImpl.class.getName());

  ServiceRef _self;

  @Inject
  @Service("bardb:///")
  private DatabaseService _db;

  @Inject
  @Service("/identity-manager")
  private IdentityManager _identityManager;

  /*
    @Inject
    @Service("pod://lucene/service")
    private com.caucho.lucene.LuceneFacade _lucene;

  */
  @Inject
  @Service("/audit")
  private AuditService _audit;

  public AuctionManagerImpl()
  {
  }

  @OnInit
  public void init(Result<Boolean> result)
  {
    _self = ServiceRef.current();

    try {
      _db.exec(
        "create table auction (id varchar primary key, title varchar, value object) with hash '/auction/$id'",
        result.of(o -> o != null));
    } catch (Exception e) {
      log.log(Level.FINE, e.getMessage(), e);
      //assume that exception is due to existing table and complete with true
      result.ok(true);
    }
  }

  @OnLookup
  public Object lookup(String path)
  {
    String id = path.substring(1);

    log.finer("lookup auction: " + id);

    return new AuctionImpl(ServiceManager.current(), _self, _db, id);
  }

  @Override
  public void create(AuctionDataInit initData,
                     Result<String> auctionId)
  {
    _identityManager.nextId(auctionId.of((id, r)
                                           -> createWithId(id,
                                                           initData,
                                                           r)));
  }

  private void createWithId(String id,
                            AuctionDataInit initData,
                            Result<String> auctionId)
  {
    Auction auction = _self.lookup("/" + id).as(Auction.class);

    auction.create(initData, auctionId.of((x, r) -> {
      index(x, initData.getTitle(), r);
    }));
  }

  private void index(String id, String title, Result<String> auctionId)
  {
    auctionId.ok(id);

    log.info(String.format("index %1$s %2$s", id, title));

    //log.warning(String.format("lucene: " + _lucene));

    //_lucene.indexText("auction", id, title, (b, t)->{});
  }

  public void find(String title, Result<String> result)
  {
    _self.save(Result.ignore());

    _db.findOne("select id from auction where title=?",
                result.of(c -> toAuctionId(c)),
                title);
  }

  private String toAuctionId(Cursor c)
  {
    log.finer(String.format("select cursor %1$s into auction id", c));

    return c != null ? c.getString(1) : null;
  }

  @Override
  public ResultStreamBuilder<String> search(String query)
  {
    throw new UnsupportedOperationException();
  }

  public void search(String query, ResultStream<String> results)
  {
    log.info(String.format("search %1$s", query));

    results.ok();
/*
    _lucene.search("auction", query, 255,
                   results.of((l, r) -> searchImp(l, r)));
*/
  }

/*
  public void searchImp(List<LuceneEntry> entries, ResultStream<String> stream)
  {
    for (LuceneEntry l : entries) {
      stream.accept(l.getExternalId());
    }

    stream.ok();
  }
*/
}
