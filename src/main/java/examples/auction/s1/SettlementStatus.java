package examples.auction.s1;

public enum SettlementStatus
{
  SETTLE_COMPLETED {

  },
  SETTLE_PENDING {

  },
  CANCEL_COMPLETED {

  },
  CANCEL_PENDING {

  };

  public void verifyIntent(SettlementIntent intent)
  {
  }

  public boolean isFinite()
  {
    return false;
  }
}
