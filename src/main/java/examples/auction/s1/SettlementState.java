package examples.auction.s1;

import java.util.LinkedList;

public class SettlementState
{
  private LinkedList<ActionEntry> _actions;

  private ActionEntry _current;

  public SettlementState()
  {
  }

  public SettlementState(Action action)
  {
    _current = new ActionEntry(action);
  }

  public void toCancel(CancelReason reason)
  {
    if (_current.getAction() == Action.CANCEL) {
    }
    else {

    }
  }

  public boolean toSettle()
  {
    if (_current.getAction())
  }

  class ActionEntry
  {
    private Action _action;
    private ActionStatus _actionStatus;
    private CancelReason _cancelReason;

    public ActionEntry()
    {
    }

    public ActionEntry(Action action)
    {
      _action = action;
    }

    public ActionStatus getActionStatus()
    {
      return _actionStatus;
    }

    public void setActionStatus(ActionStatus actionStatus)
    {
      _actionStatus = actionStatus;
    }

    public CancelReason getCancelReason()
    {
      return _cancelReason;
    }

    public void setCancelReason(CancelReason cancelReason)
    {
      _cancelReason = cancelReason;
    }

    public Action getAction()
    {
      return _action;
    }
  }

  public enum Action
  {
    SETTLE,
    CANCEL
  }

  public enum ActionStatus
  {
    COMPLETED,
    PENDING,
    CANCELLED
  }

  public enum CancelReason
  {
    REJECTED_PAYMENT,
    REJECTED_USER,
    REJECTED_AUCTION,
    USER_CANCEL
  }

}
