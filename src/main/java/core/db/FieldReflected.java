package core.db;

import io.baratine.db.Cursor;

import java.lang.reflect.Field;

class FieldReflected implements FieldDesc
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
    return _field.getAnnotation(Id.class) != null;
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
    return RepositoryImpl.getColumnType(_field.getType());
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
