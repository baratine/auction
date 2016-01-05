package examples.auction;

import com.caucho.lucene.LuceneEntry;
import com.caucho.lucene.LuceneException;
import com.caucho.lucene.LuceneFacade;
import io.baratine.service.Result;
import io.baratine.service.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("public:///service")
public class MockLuceneService implements LuceneFacade
{
  @Override
  public void indexFile(String s, String s1, Result<Boolean> result)
    throws LuceneException
  {
    result.ok(true);
  }

  @Override
  public void indexText(String s, String s1, String s2, Result<Boolean> result)
    throws LuceneException
  {
    result.ok(true);
  }

  @Override
  public void indexMap(String s,
                       String s1,
                       Map<String,Object> map,
                       Result<Boolean> result) throws LuceneException
  {
    result.ok(true);
  }

  @Override
  public void search(String s,
                     String s1,
                     int i,
                     Result<List<LuceneEntry>> result) throws LuceneException
  {
    List<LuceneEntry> entries = new ArrayList<>();

    result.ok(entries);
  }

  @Override
  public void delete(String s, String s1, Result<Boolean> result)
    throws LuceneException
  {
    result.ok(true);
  }

  @Override
  public void clear(String s, Result<Void> result) throws LuceneException
  {
    result.ok(null);
  }
}
