package examples.auction;

public class CreditCard
{
  private String _type;
  private String _num;
  private String _cvv;
  private int _expMonth;
  private int _expYear;

  public CreditCard(String type,
                    String num,
                    String cvv,
                    int expMonth,
                    int expYear)
  {
    _type = type;
    _num = num;
    _cvv = cvv;
    _expMonth = expMonth;
    _expYear = expYear;
  }

  public String getType()
  {
    return _type;
  }

  public String getNum()
  {
    return _num;
  }

  public String getCvv()
  {
    return _cvv;
  }

  public int getExpMonth()
  {
    return _expMonth;
  }

  public int getExpYear()
  {
    return _expYear;
  }
}
