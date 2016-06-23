package examples.auction;

import java.util.List;

import io.baratine.service.Result;
import io.baratine.web.Body;

public interface AuctionSession
{
  void createUser(@Body UserInitData user, Result<WebUser> result);

  void login(String user, String password, Result<Boolean> result);

  void getUser(Result<WebUser> result);

  void getAuction(String id, Result<WebAuction> result);

  void findAuction(String title, Result<Auction> result);

  void searchAuctions(String query, Result<List<WebAuction>> result);

  void addAuctionListener(String idAuction,
                          Result<Boolean> result);

  void logout(Result<Boolean> result);

  interface WebAuctionUpdateListener
  {
    void auctionUpdated(WebAuction auction);
  }

  public static class WebAuction
  {
    private String id;
    private String title;
    private long bid;
    private String state;

    public WebAuction()
    {
    }

    private WebAuction(String id, String title, long bid, String state)
    {
      this.id = id;
      this.title = title;
      this.bid = bid;
      this.state = state;
    }

    public static WebAuction of(AuctionData auction)
    {
      Auction.Bid bid = auction.getLastBid();
      int price = bid != null ? bid.getBid() : auction.getStartingBid();

      WebAuction webAuction
        = new WebAuction(auction.getEncodedId(),
                         auction.getTitle(),
                         price,
                         auction.getState().toString());

      return webAuction;
    }

    public String getId()
    {
      return id;
    }

    public String getTitle()
    {
      return title;
    }

    public long getBid()
    {
      return bid;
    }

    public String getState()
    {
      return state;
    }

    @Override
    public String toString()
    {
      return this.getClass().getSimpleName() + "["
             + title
             + ", "
             + bid
             + ", "
             + state
             + ']';
    }
  }

  class UserInitData
  {
    private String user;
    private String password;

    private boolean isAdmin;

    public UserInitData()
    {
    }

    public UserInitData(String user, String password, boolean isAdmin)
    {
      this.user = user;
      this.password = password;
      this.isAdmin = isAdmin;
    }

    public String getUser()
    {
      return user;
    }

    public String getPassword()
    {
      return password;
    }

    public boolean isAdmin()
    {
      return isAdmin;
    }

    @Override
    public String toString()
    {
      return this.getClass().getSimpleName() + "["
             + user
             + ", "
             + password
             + ']';
    }
  }

  class WebUser
  {
    private String id;
    private String user;

    public WebUser()
    {
    }

    private WebUser(String id, String user)
    {
      this.id = id;
      this.user = user;
    }

    public static WebUser of(UserData user)
    {
      return new WebUser(user.getEncodedId(), user.getName());
    }

    public String getName()
    {
      return user;
    }

    @Override
    public String toString()
    {
      return this.getClass().getSimpleName() + "["
             + user
             + ", "
             + id
             + ']';
    }
  }
}
