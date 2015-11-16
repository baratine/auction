package examples.auction.s1;

public enum SettlementIntent
{
  SETTLE,
  CANCEL {
    @Override
    public void verifyIntent(SettlementIntent intent)
    {
      if (intent == SETTLE)
        throw new IllegalStateException(String.format(
          "can't change intent %1$s to intent %2$s",
          this,
          intent));
    }
  };

  public void verifyIntent(SettlementIntent intent)
  {
  }
}
