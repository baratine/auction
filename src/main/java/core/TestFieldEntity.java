package core;

@Table(name = "TEST_ENTITY")
public class TestFieldEntity
{
  @Column(name = "id", pk = true)
  private String _id;

  @Column(name = "data")
  private String _data;

  public TestFieldEntity()
  {
  }

  public String getId()
  {
    return _id;
  }

  public void setId(String id)
  {
    _id = id;
  }

  public String getData()
  {
    return _data;
  }

  public void setData(String data)
  {
    _data = data;
  }
}
