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



    System.out.println(r.createDdl());
    System.out.println(r.getInsertSql());
    System.out.println(r.getSelectOneSql());

    r.save(e);
  }
}
