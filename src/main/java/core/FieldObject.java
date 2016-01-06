package core;

public class FieldObject implements FieldDesc
{
  @Override
  public boolean isPk()
  {
    return false;
  }

  @Override
  public String getName()
  {
    return "value";
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
}
