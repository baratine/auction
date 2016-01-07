package core.db;

import io.baratine.service.Result;
import io.baratine.stream.ResultStreamBuilder;

import java.io.Serializable;

public interface Repository<T, ID extends Serializable>
{
  <S extends T> void save(S entity, Result<Boolean> result);

  void findOne(ID id, Result<T> result);

  ResultStreamBuilder<T> findMatch(String[] columns, Object[] values);

  ResultStreamBuilder<T> find(Iterable<ID> ids);

  ResultStreamBuilder<T> findAll();

  void delete(ID id, Result<Boolean> result);

  void delete(Iterable<ID> ids, Result<Boolean> result);
}