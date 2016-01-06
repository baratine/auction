package core;

public class TestEntityProgram
{
  public static void main(String[] args) throws ClassNotFoundException
  {
    RepositoryImpl<TestEntity,String> r = new RepositoryImpl<>(TestEntity.class,
                                                           String.class);

    r.init();

    TestEntity e = new TestEntity();

    r.save(e);

    System.out.println(r.createDdl());
    System.out.println(r.getInsertSql());
  }
}
