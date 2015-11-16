package examples.auction.s1;

import java.util.Stack;

public class SettlementState
{
  private Stack<ActionEntry> _actions = new Stack<>();

  public SettlementState()
  {
  }

  public SettlementState(Action action)
  {
    _actions.push(new ActionEntry(action));
  }

  public void toCancel(ActionReason reason)
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
    private ActionReason _actionReason;

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

    public ActionReason getActionReason()
    {
      return _actionReason;
    }

    public void setActionReason(ActionReason actionReason)
    {
      _actionReason = actionReason;
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

  public enum ActionReason
  {
    REJECTED_PAYMENT,
    REJECTED_USER,
    REJECTED_AUCTION,
    USER_CANCEL
  }

}
