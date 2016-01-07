package core.db;

import io.baratine.db.Cursor;

import java.util.Objects;

public class FieldObject implements FieldDesc
{
  private Column _column;
  private Class<?> _type;

  public FieldObject(Class<?> type, Column column)
  {
    Objects.requireNonNull(type);
    Objects.requireNonNull(column);

    _type = type;
    _column = column;
  }

  @Override
  public boolean isPk()
  {
    return false;
  }

  @Override
  public String getName()
  {
    String name = _column.name();

    if (name == null) {
      name = _type.getSimpleName();
    }

    return name;
  }

  @Override
  public String getSqlType()
  {
    return "object";
  }

  @Override
  public Object getValue(Object t)
  {
    return t;
  }

  @Override
  public void setValue(Object target, Cursor cursor, int index)
  {
    throw new IllegalStateException();
  }
}
