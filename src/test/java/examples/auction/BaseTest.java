package examples.auction;

import io.baratine.core.Lookup;
import io.baratine.core.ResultFuture;
import io.baratine.core.ServiceRef;

import java.util.concurrent.TimeUnit;

public class BaseTest
{
  @Lookup("pod://user/user")
  UserManager _users;

  @Lookup("pod://user/user")
  ServiceRef _usersServiceRef;

  protected String userCreate(String userName, String password)
  {
    ResultFuture<String> userId = new ResultFuture<>();

    _users.create(userName, password, userId);

    return userId.get(10, TimeUnit.SECONDS);
  }

  private User lookupUser(String id)
  {
    return _usersServiceRef.lookup(id).as(User.class);
  }

  protected User findUser(String name)
  {
    ResultFuture<String> futureUserId = new ResultFuture<>();

    _users.find(name, futureUserId);

    String userId = futureUserId.get(2, TimeUnit.SECONDS);

    return _usersServiceRef.lookup("/" + userId).as(User.class);
  }

  protected boolean userAuthenticate(User user, String password)
  {
    ResultFuture<Boolean> result = new ResultFuture<>();

    user.authenticate(password, result);

    return result.get(10, TimeUnit.SECONDS);
  }

  protected boolean open(Auction auction)
  {
    ResultFuture<Boolean> result = new ResultFuture<>();

    auction.open(result);

    return result.get();
  }

  protected boolean close(Auction auction)
  {
    ResultFuture<Boolean> result = new ResultFuture<>();

    auction.close(result);

    return result.get();
  }

  protected AuctionDataPublic getAuctionDataPublic(Auction auction)
  {
    ResultFuture<AuctionDataPublic> result = new ResultFuture<>();

    auction.get(result);

    return result.get();
  }

  protected boolean bid(Auction auction, String userId, int bid)
  {
    ResultFuture<Boolean> result = new ResultFuture<>();

    auction.bid(userId, bid, result);

    return result.get();
  }

  protected UserDataPublic getUserDataPublic(UserManager userManager,
                                             String userId)
  {
    ResultFuture<UserDataPublic> result = new ResultFuture<>();

    userManager.getUser(userId, result);

    return result.get();
  }
/*
  protected UserDataPublic getUserDataPublic(ChannelAuction channel)
  {
    ResultFuture<UserDataPublic> result = new ResultFuture<>();

    channel.getUser(result);

    return result.get();
  }

  public boolean createUser(ChannelAuction channel,
                            String user,
                            String password)
  {
    ResultFuture<Boolean> result = new ResultFuture<>();

    channel.createUser(user, password, result);

    return result.get();
  }

  protected boolean login(ChannelAuction channel, String user, String password)
  {
    ResultFuture<Boolean> result = new ResultFuture<>();

    channel.login(user, password, result);

    return result.get();
  }

  protected String createAuction(ChannelAuction channel, String title, int bid)
  {
    ResultFuture<String> result = new ResultFuture<>();

    channel.createAuction(title, bid, result);

    return result.get();
  }

  protected AuctionDataPublic getAuctionDataPublic(ChannelAuction channel,
                                                   String id)
  {
    ResultFuture<AuctionDataPublic> result = new ResultFuture<>();

    channel.getAuction(id, result);

    return result.get();
  }

  protected String findAuction(ChannelAuction channel, String title)
  {
    ResultFuture<String> result = new ResultFuture<>();

    channel.findAuction(title, result);

    return result.get();
  }

  protected boolean bid(ChannelAuction channel, String auctionId, int bid)
  {
    ResultFuture<Boolean> result = new ResultFuture<>();

    channel.bidAuction(auctionId, bid, result);

    return result.get();
  }

  protected boolean setListener(ChannelAuction channel,
                                ChannelListener listener)
  {
    ResultFuture<Boolean> result = new ResultFuture<>();

    channel.setListener(listener, result);

    return result.get();
  }

  boolean addAuctionListener(ChannelAuction channel, String id)
  {
    ResultFuture<Boolean> result = new ResultFuture<>();

    channel.addAuctionListener(id, result);

    return result.get();
  }*/

  private void sleep(int seconds)
  {
    try {
      Thread.sleep(seconds * 1000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
