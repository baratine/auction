package core;

import io.baratine.db.Cursor;
import io.baratine.db.DatabaseService;
import io.baratine.service.Result;
import io.baratine.service.ServiceManager;
import io.baratine.stream.ResultStreamBuilder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RepositoryImpl<T, ID extends Serializable>
  implements Repository<T,ID>
{
  private Class<ID> _idClass;
  private Class<T> _entityClass;

  private EntityDesc<T> _entityDesc;

  private DatabaseService _db;

  public RepositoryImpl(Class<T> entityClass, Class<ID> idClass)
    throws ClassNotFoundException
  {
    _entityClass = entityClass;
    _idClass = idClass;
  }

  public void init()
  {
    Table table = _entityClass.getAnnotation(Table.class);

    _entityDesc = new EntityDesc<>(_entityClass, table);

    _db = ServiceManager.current()
                        .lookup("bardb:///")
                        .as(DatabaseService.class);
  }

  @Override
  public <S extends T> void save(S entity, Result<Boolean> result)
  {
    Object[] values = new Object[_entityDesc.getSize()];

    for (int i = 0; i < values.length; i++) {
      Object value = _entityDesc.getValue(i, entity);

      values[i] = value;
    }

    System.out.println("RepositoryImpl.save: "
                       + getInsertSql()
                       + ": "
                       + Arrays.asList(values));

    _db.exec(getInsertSql(), Result.ignore(), values);

    result.ok(true);
  }

  @Override
  public void findOne(ID id, Result<T> result)
  {
    _db.findOne(getSelectOneSql(), result.of((c, r) -> readObject(c, r)), id);
  }

  private void readObject(Cursor c, Result<T> result)
  {
    if (c == null)
      result.ok(null);

    try {

      T t = _entityDesc.readObject(c);

      result.ok(t);
    } catch (ReflectiveOperationException e) {

      //TODO log.log()
      result.fail(e);
    }
  }

  @Override
  public ResultStreamBuilder<T> find(Iterable<ID> ids)
  {
    return null;
  }

  @Override
  public ResultStreamBuilder<T> findAll()
  {
    return null;
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

    head.append(where);

    return head.toString();
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

  static class EntityDesc<T>
  {
    private final Table _table;
    private final FieldDesc[] _fields;
    private Class<T> _class;

    public EntityDesc(Class<T> type, Table table)
    {
      Objects.requireNonNull(type);
      Objects.requireNonNull(table);

      _class = type;
      _table = table;

      List<FieldDesc> fields = new ArrayList<>();

      boolean hasPk = false;
      for (Field field : _class.getDeclaredFields()) {
        Column column = field.getAnnotation(Column.class);
        if (column == null)
          continue;

        FieldDesc f = new FieldReflected(field, column);

        if (f.isPk())
          hasPk = true;

        fields.add(f);
      }

      if (!hasPk) {
        throw new IllegalStateException(String.format(
          "%1$s must define a primary key",
          type));
      }

      Column objColumn = type.getAnnotation(Column.class);

      if (objColumn != null) {
        fields.add(new FieldObject(type, objColumn));
      }

      _fields = fields.toArray(new FieldDesc[fields.size()]);
    }

    public FieldDesc[] getFields()
    {
      return _fields;
    }

    public String getTableName()
    {
      return _table.name();
    }

    public int getSize()
    {
      return _fields.length;
    }

    public Object getValue(int index, T t)
    {
      return _fields[index].getValue(t);
    }

    public T readObject(Cursor cursor) throws ReflectiveOperationException
    {
      FieldObject fieldObject = null;

      int index = 0;
      for (int i = 0; i < _fields.length; i++) {
        FieldDesc field = _fields[i];

        if (!field.isPk())
          index++;

        if (field instanceof FieldObject) {
          fieldObject = (FieldObject) field;
          break;
        }
      }

      T t;

      if (fieldObject == null) {
        t = createFromClass();
        fillIn(cursor, t);
      }
      else {
        t = createFromCursor(cursor, index);
      }

      return t;
    }

    private void fillIn(Cursor cursor, T t)
    {
      int index = 0;
      for (int i = 0; i < _fields.length; i++) {
        FieldDesc field = _fields[i];

        if (field.isPk()) {
          continue;
        }
        else {
          index++;
        }

        field.setValue(t, cursor, index);
      }
    }

    private T createFromClass()
      throws ReflectiveOperationException
    {
      return _class.newInstance();
    }

    private T createFromCursor(Cursor cursor, int index)
    {
      return (T) cursor.getObject(index);
    }
  }

  static class FieldReflected implements FieldDesc
  {
    private final Field _field;
    private final Column _column;

    public FieldReflected(Field field, Column column)
    {
      _field = field;
      _column = column;

      _field.setAccessible(true);
    }

    @Override
    public boolean isPk()
    {
      return _column.pk();
    }

    @Override
    public String getName()
    {
      String name = _column.name();

      if (name == null) {

      }

      return name;
    }

    @Override
    public String getSqlType()
    {
      return getColumnType(_field.getType());
    }

    @Override
    public Object getValue(Object t)
    {
      try {
        return _field.get(t);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException();
      }
    }

    @Override
    public void setValue(Object target, Cursor cursor, int index)
    {

    }
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
