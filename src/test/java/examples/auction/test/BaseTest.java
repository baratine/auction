package examples.auction.test;

import examples.auction.Auction;
import examples.auction.AuctionDataPublic;
import examples.auction.UserDataPublic;
import examples.auction.UserService;
import io.baratine.core.Lookup;
import io.baratine.core.ResultFuture;
import io.baratine.core.ServiceRef;

import java.util.concurrent.TimeUnit;

public class BaseTest
{
  @Lookup("pod://user/user")
  UserService _userService;

  @Lookup("pod://user/user")
  ServiceRef _userServiceRef;

  protected boolean userCreate(String userName, String password)
  {
    ResultFuture<Boolean> future = new ResultFuture<>();

    _userService.createUser(userName, password, future);

    return future.get(10, TimeUnit.SECONDS);
  }

  protected UserDataPublic userAuthenticate(String userName, String password)
  {
    ResultFuture<UserDataPublic> future = new ResultFuture<>();

    _userService.authenticate(userName, password, future);

    return future.get(10, TimeUnit.SECONDS);
  }

  protected UserDataPublic userGetById(String id)
  {
    ResultFuture<UserDataPublic> future = new ResultFuture<>();

    _userService.getUser(id, future);

    return future.get(10, TimeUnit.SECONDS);
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

  protected UserDataPublic getUserDataPublic(UserService userService,
                                             String userId)
  {
    ResultFuture<UserDataPublic> result = new ResultFuture<>();

    userService.getUser(userId, result);

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
