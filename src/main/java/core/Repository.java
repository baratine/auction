package core;

import java.io.Serializable;

public interface Repository<T, ID extends Serializable>
{
  <S extends T> S save(S entity);

  T findOne(ID id);

  Iterable<T> find(Iterable<ID> ids);

  Iterable<T> findAll();

  void delete(ID id);

  void delete(Iterable<ID> ids);
}