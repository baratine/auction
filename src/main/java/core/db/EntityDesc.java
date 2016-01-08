package core.db;

import io.baratine.db.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class EntityDesc<T>
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

    for (Field field : _class.getDeclaredFields()) {
      if (!isPersistent(field))
        continue;

      Column column = field.getAnnotation(Column.class);

      if (column == null)
        continue;

      FieldDesc f = new FieldReflected(field, column);

      fields.add(f);
    }

    List<FieldDesc> pks = new ArrayList<>();

    for (FieldDesc field : fields) {
      if (field.isPk())
        pks.add(field);
    }

    if (pks.size() == 0) {
      throw new IllegalStateException(String.format(
        "%1$s must define a primary key",
        type));
    }
    else if (pks.size() > 1)
      throw new IllegalStateException(String.format(
        "too many fields declare primary key %1$s",
        pks.toString()));

    Column objColumn = type.getAnnotation(Column.class);

    if (objColumn != null) {
      fields.add(new FieldObject(type, objColumn));
    }

    _fields = fields.toArray(new FieldDesc[fields.size()]);
  }

  private boolean isPersistent(Field field)
  {
    int mod = field.getModifiers();

    if (Modifier.isStatic(mod))
      return false;
    else if (Modifier.isTransient(mod))
      return false;
    return true;
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

  public T readObject(Cursor cursor, boolean isInitPk)
    throws ReflectiveOperationException
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
      fillIn(cursor, t, isInitPk);
    }
    else {
      t = createFromCursor(cursor, index);
    }

    return t;
  }

  private void fillIn(Cursor cursor, T t, boolean isInitPk)
  {
    int index = 0;
    for (int i = 0; i < _fields.length; i++) {
      FieldDesc field = _fields[i];

      if (!isInitPk && field.isPk()) {
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
    T t = null;

    if (cursor != null)
      t = (T) cursor.getObject(index);

    return t;
  }

  @Override
  public String toString()
  {
    return EntityDesc.class.getSimpleName()
           + "["
           + _class
           + ':'
           + getTableName()
           + "]";
  }
}
