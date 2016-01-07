package core;

@Table(name = "TEST_ENTITY")
public class TestFieldEntity
{
  @Column(name = "id", pk = true)
  private String _id = "-id-";

  @Column(name = "data")
  private String _data = "-data-";

  @Column(name = "data1")
  private String _data1 = "-data1-";

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

  public String getData1()
  {
    return _data1;
  }

  public void setData1(String data1)
  {
    _data1 = data1;
  }
}
