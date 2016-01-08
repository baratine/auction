package core.db;

import io.baratine.db.Cursor;

interface FieldDesc
{
  boolean isPk();

  String getName();

  String getSqlType();

  Object getValue(Object t);

  void setValue(Object target, Cursor cursor, int index);
}
