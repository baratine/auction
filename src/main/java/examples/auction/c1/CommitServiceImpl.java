package examples.auction.c1;

import examples.auction.User;
import examples.auction.UserManager;
import io.baratine.core.Lookup;
import io.baratine.core.Result;
import io.baratine.core.ServiceRef;

import javax.inject.Inject;

public class CommitServiceImpl implements CommitService
{
  @Inject
  @Lookup("pod://user/user")
  ServiceRef _userRef;

  @Inject
  @Lookup("pod://auction/auction")
  ServiceRef _auctionManager;

  @Override
  public void commit(String auctionId, String userId, Result<Boolean> result)
  {
   //  User user = _userRef.
  }
}
