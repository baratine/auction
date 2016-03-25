package examples.auction;

import java.util.List;

import io.baratine.service.Result;
import io.baratine.web.Body;
import io.baratine.web.Form;

public interface AuctionSession
{
  void createUser(@Body UserInitData user, Result<WebUser> result);

  void login(@Body Form login, Result<Boolean> result);

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

  class WebAuction
  {
    private String id;
    private String title;
    private long bid;
    private String state;

    public WebAuction()
    {
    }

    public WebAuction(String id, String title, long bid, String state)
    {
      this.id = id;
      this.title = title;
      this.bid = bid;
      this.state = state;
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
    String id;
    String user;

    public WebUser()
    {
    }

    public WebUser(String id, String user)
    {
      this.id = id;
      this.user = user;
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
