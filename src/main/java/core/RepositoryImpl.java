package core;

import io.baratine.db.DatabaseService;
import io.baratine.service.Result;
import io.baratine.service.ServiceManager;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
  public <S extends T> S save(S entity)
  {
    Object[] values = new Object[_entityDesc.getSize()];

    for (int i = 0; i < values.length; i++) {
      Object value = _entityDesc.getValue(i, entity);

      values[i] = value;
    }

    _db.exec(getInsertSql(), Result.ignore(), values);

    return entity;
  }

  @Override
  public T findOne(ID id)
  {
    return null;
  }

  @Override
  public Iterable<T> find(Iterable<ID> ids)
  {
    return null;
  }

  @Override
  public Iterable<T> findAll()
  {
    return null;
  }

  @Override
  public void delete(ID id)
  {

  }

  @Override
  public void delete(Iterable<ID> entities)
  {

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

      if (fields.size() == 1) {
        fields.add(new FieldObject());
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

    public Object getValue(int field, T t)
    {
      throw new IllegalStateException();
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

