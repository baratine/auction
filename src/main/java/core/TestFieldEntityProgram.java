package core;

public class TestFieldEntityProgram
{
  public static void main(String[] args) throws ClassNotFoundException
  {
    RepositoryImpl<TestFieldEntity,String> r = new RepositoryImpl<>(
      TestFieldEntity.class,
      String.class);

    r.init();

    TestFieldEntity e = new TestFieldEntity();

    r.save(e);

    System.out.println(r.createDdl());
    System.out.println(r.getInsertSql());
  }
}
