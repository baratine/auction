package examples.auction;

import java.util.List;

import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.web.Body;
import io.baratine.web.Form;

/**
 * User visible channel facade at session://web/auction-session
 */
public interface AuctionSession
{
  void createUser(@Body UserInitData user, Result<WebUser> result);

  void login(@Body Form login, Result<Boolean> result);

  void getUser(Result<UserData> result);

  void createAuction(Form form, Result<String> result);

  void getAuction(String id, Result<AuctionDataPublic> result);

  void findAuction(String title, Result<Auction> result);

  void search(String query, Result<List<String>> result);

  void bidAuction(String id, int bid, Result<Boolean> result);

  void setListener(@Service ChannelListener listener,
                   Result<Boolean> result);

  void addAuctionListener(String idAuction,
                          Result<Boolean> result);

  void logout(Result<Boolean> result);

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
