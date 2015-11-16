package examples.auction.s1;

import java.util.LinkedList;

public class SettlementState
{
  private LinkedList<Entry> _list = new LinkedList<>();

  class Entry
  {
    private Intent _intent;
    private Status _status;
    private Error _error;
  }

  public enum Intent
  {
    SETTLE,
    CANCEL
  }

  public enum Status
  {
    SETTLE_COMPLETED,
    SETTLE_PENDING,
    SETTLE_ERROR,
    CANCEL_COMPLETED,
    CANCEL_PENDING,
    CANCEL_ERROR
  }

  public enum Error
  {
    REJECTED_PAYMENT,
    REJECTED_USER,
    REJECTED_AUCTION
  }
}
