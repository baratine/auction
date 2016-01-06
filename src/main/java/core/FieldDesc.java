package core;

public interface FieldDesc
{
  boolean isPk();

  String getName();

  String getSqlType();

  Object getValue(Object t);
}
