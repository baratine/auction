package examples.auction.s1;

public class SettlementState
{
  private Action _settleAction;
  private Action _cancelAction;

  public SettlementState()
  {
  }

  public void toSettle()
  {
    if (_cancelAction != null)
      throw new IllegalStateException();

    if (_settleAction != null)
      throw new IllegalStateException();

    _settleAction = new Action(ActionIntent.SETTLE);
  }

  public ActionIntent getIntent()
  {
    if (_cancelAction != null)
      return ActionIntent.CANCEL;
    else if (_settleAction != null)
      return ActionIntent.SETTLE;
    else
      return ActionIntent.NULL;
  }

  public void toCancel(ActionCause reason)
  {
    if (_cancelAction != null)
      throw new IllegalStateException();

    _cancelAction = new Action(ActionIntent.CANCEL);
  }

  class Action
  {
    private ActionIntent _actionIntent;
    private ActionStatus _actionStatus;
    private ActionCause _actionCause;

    public Action()
    {
    }

    public Action(ActionIntent actionIntent)
    {
      _actionIntent = actionIntent;
    }

    public ActionStatus getActionStatus()
    {
      return _actionStatus;
    }

    public void setActionStatus(ActionStatus actionStatus)
    {
      _actionStatus = actionStatus;
    }

    public ActionCause getActionCause()
    {
      return _actionCause;
    }

    public void setActionCause(ActionCause actionCause)
    {
      _actionCause = actionCause;
    }

    public ActionIntent getActionIntent()
    {
      return _actionIntent;
    }
  }

  public enum ActionIntent
  {
    SETTLE,
    CANCEL,
    NULL
  }

  public enum ActionStatus
  {
    COMPLETED,
    PENDING,
    CANCELLED
  }

  public enum ActionCause
  {
    REJECTED_PAYMENT,
    REJECTED_USER,
    REJECTED_AUCTION,
    REFUND
  }
}
