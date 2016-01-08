package core.db;

import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.service.Result;
import io.baratine.service.ResultStream;
import io.baratine.stream.ResultStreamBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepositoryImpl<T, ID extends Serializable>
  implements Repository<T,ID>
{
  private static final Logger log
    = Logger.getLogger(RepositoryImpl.class.getName());

  private Class<ID> _idClass;
  private Class<T> _entityClass;

  private EntityDesc<T> _entityDesc;

  private DatabaseService _db;

  public RepositoryImpl(Class<T> entityClass,
                        Class<ID> idClass,
                        DatabaseService db)
  {
    _entityClass = entityClass;
    _idClass = idClass;
    _db = db;
  }

  public void init()
  {
    Table table = _entityClass.getAnnotation(Table.class);

    _entityDesc = new EntityDesc<>(_entityClass, table);

    _db.exec(createDdl(), Result.ignore());
  }

  @Override
  public <S extends T> void save(S entity, Result<Boolean> result)
  {
    log.log(Level.FINER, String.format("saving entity %1$s", entity));

    Object[] values = new Object[_entityDesc.getSize()];

    for (int i = 0; i < values.length; i++) {
      Object value = _entityDesc.getValue(i, entity);

      values[i] = value;
    }

    _db.exec(getInsertSql(), result.of(o -> {
      log.log(Level.FINER, String.format("entity %1$s is saved", entity));
      return true;
    }), values);
  }

  @Override
  public void findOne(ID id, Result<T> result)
  {
    log.log(Level.FINER, String.format("loading entity %1$s with for id %2$s",
                                       _entityDesc,
                                       id));

    _db.findOne(getSelectOneSql(), result.of((c, r) -> readObject(c, r)), id);
  }

  @Override
  public ResultStreamBuilder<T> findMatch(String[] columns, Object[] values)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @param columns
   * @param values
   * @param stream
   * @see #findMatch(String[], Object[])
   */
  public void findMatch(String[] columns,
                        Object[] values,
                        ResultStream<T> stream)
  {
    if (columns.length != values.length)
      throw new IllegalArgumentException();

    StringBuilder sql = getWildSelect();

    sql.append(" where ");

    for (int i = 0; i < columns.length; i++) {
      String column = columns[i];
      sql.append(column).append("=?");

      if ((i + 1) < columns.length)
        sql.append(" and ");
    }

    _db.findAll(sql.toString(),
                stream.of((i, r) -> {readObjects(i, r);}),
                values);
  }

  private void readObjects(Iterable<Cursor> it, ResultStream<T> r)
  {
    try {
      for (Cursor c : it) {
        T t = _entityDesc.readObject(c, true);

        r.accept(t);
      }

      r.ok();
    } catch (ReflectiveOperationException e) {
      //TODO log
      r.fail(e);
    }
  }

  private void readObject(Cursor c, Result<T> result)
  {
    if (c == null) {
      log.log(Level.FINER, String.format("%1$s cursor is null", _entityDesc));

      result.ok(null);
    }

    try {
      T t = _entityDesc.readObject(c, false);

      log.log(Level.FINER, String.format("loaded %1$s", t));

      result.ok(t);
    } catch (ReflectiveOperationException e) {

      //TODO log.log()
      result.fail(e);
    }
  }

  @Override
  public ResultStreamBuilder<T> find(Iterable<ID> ids)
  {
    throw new UnsupportedOperationException();
  }

  public void find(Iterable<ID> ids, ResultStream<T> stream)
  {
    //TODO
  }

  @Override
  public ResultStreamBuilder<T> findAll()
  {
    throw new UnsupportedOperationException();
  }

  public void findAll(ResultStream<T> stream)
  {
    StringBuilder sql = getWildSelect();

    _db.findAll(sql.toString(), stream.of(this::readObjects));
  }

  @Override
  public void delete(ID id, Result<Boolean> result)
  {
    _db.exec(getDeleteSql(), Result.ignore(), id);
  }

  @Override
  public void delete(Iterable<ID> ids, Result<Boolean> result)
  {
    for (ID entity : ids) {
      delete(entity, null);
    }
  }

  public String getInsertSql()
  {
    StringBuilder head = new StringBuilder("insert into ")
      .append(_entityDesc.getTableName())
      .append('(');

    StringBuilder tail = new StringBuilder(") values (");

    FieldDesc[] fields = _entityDesc.getFields();
    for (int i = 0; i < fields.length; i++) {
      FieldDesc field = _entityDesc.getFields()[i];

      head.append(field.getName());

      tail.append('?');
      if ((i + 1) < fields.length) {
        head.append(", ");
        tail.append(", ");
      }
    }

    tail.append(')');
    head.append(tail);

    return head.toString();
  }

  public String getSelectOneSql()
  {
    StringBuilder head = new StringBuilder("select ");
    StringBuilder where = new StringBuilder(" where ");

    FieldDesc[] fields = _entityDesc.getFields();

    for (int i = 0; i < fields.length; i++) {
      FieldDesc field = fields[i];

      if (field.isPk()) {
        where.append(field.getName()).append(" = ?");
        continue;
      }

      head.append(field.getName());

      if ((i + 1) < fields.length && !fields[i + 1].isPk())
        head.append(", ");
    }

    head.append(" from ").append(_entityDesc.getTableName());

    head.append(where);

    return head.toString();
  }

  public StringBuilder getWildSelect()
  {
    StringBuilder sql = new StringBuilder("select ");

    FieldDesc[] fields = _entityDesc.getFields();

    for (int i = 0; i < fields.length; i++) {
      FieldDesc field = fields[i];
      sql.append(field.getName());

      if ((i + 1) < fields.length)
        sql.append(", ");
    }

    return sql;
  }

  public String getDeleteSql()
  {
    StringBuilder sql = new StringBuilder("delete from ")
      .append(_entityDesc.getTableName())
      .append(" where ");

    for (FieldDesc field : _entityDesc.getFields()) {
      if (field.isPk()) {
        sql.append(field.getName());
        break;
      }

    }

    sql.append(" = ?");

    return sql.toString();
  }

  public String createDdl()
  {
    StringBuilder createDdl = new StringBuilder("create table ")
      .append(_entityDesc.getTableName()).append('(');

    FieldDesc[] fields = _entityDesc.getFields();

    for (int i = 0; i < fields.length; i++) {
      FieldDesc field = _entityDesc.getFields()[i];

      createDdl.append(field.getName())
               .append(' ')
               .append(field.getSqlType());

      if (field.isPk()) {
        createDdl.append(" primary key");
      }

      if ((i + 1) < fields.length) {
        createDdl.append(", ");
      }
    }

    createDdl.append(')');

    return createDdl.toString();
  }

  public static String getColumnType(Class type)
  {
    String sqlType = typeMap.get(type);

    if (sqlType == null)
      sqlType = "object";

    return sqlType;
  }

  private final static Map<Class,String> typeMap = new HashMap<>();

  static {
    typeMap.put(byte.class, "integer");
    typeMap.put(Byte.class, "integer");

    typeMap.put(short.class, "integer");
    typeMap.put(Short.class, "integer");

    typeMap.put(char.class, "char");
    typeMap.put(Character.class, "char");

    typeMap.put(int.class, "integer");
    typeMap.put(Integer.class, "integer");

    typeMap.put(long.class, "long");
    typeMap.put(Long.class, "long");

    typeMap.put(float.class, "float");
    typeMap.put(Float.class, "float");

    typeMap.put(double.class, "double");
    typeMap.put(Double.class, "double");

    typeMap.put(String.class, "varchar");
  }
}
