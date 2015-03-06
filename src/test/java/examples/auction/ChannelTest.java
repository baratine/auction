package examples.auction;

import com.caucho.junit.RunnerBaratine;
import org.junit.runner.RunWith;

/**
 * The AuctionChannel is the client-visible facade to the auction system.
 * The Channel is used for encapsulation and security. The client doesn't need
 * to see the internal services directly.
 */
@RunWith(RunnerBaratine.class)
/*
@ConfigurationBaratine(services = {UserResource.class,
                                   ChannelAuctionResource.class,
                                   AuctionResource.class},
  testTime = 0)
*/
public class ChannelTest
{
/*
  @Inject
  ServiceManager _auctionPod;

  @Inject RunnerBaratine _testContext;

  */
/**
   * User create correctly sets the user name.
   *//*

  @Test
  public void userCreate()
  {
    ChannelAuction channel = _auctionPod.lookup("channel:///auction-channel/test")
                                     .as(ChannelAuction.class);

    Assert.assertNotNull(channel);

    boolean result = createUser(channel, "Spock", "passwd");

    Assert.assertTrue(result);

    UserDataPublic data = getUserDataPublic(channel);
    Assert.assertEquals("Spock", data.getName());
  }

  */
/**
   * Login correctly sets user name.
   *//*

  @Test
  public void userLogin()
  {
    ChannelAuction channelCreate
      = _auctionPod.lookup("channel:///auction-channel/create")
                .as(ChannelAuction.class);

    Assert.assertNotNull(channelCreate);

    boolean result = createUser(channelCreate, "Spock", "passwd");
    Assert.assertTrue(result);

    ChannelAuction channelLogin
      = _auctionPod.lookup("channel:///auction-channel/test")
                .as(ChannelAuction.class);

    Assert.assertNotNull(channelLogin);

    result = login(channelLogin, "Spock", "passwd");
    Assert.assertTrue(result);

    UserDataPublic data = getUserDataPublic(channelLogin);
    Assert.assertEquals("Spock", data.getName());
  }

  */
/**
   * creates an auction
   *//*

  @Test
  public void auctionCreate()
  {
    ChannelAuction channelCreate
      = _auctionPod.lookup("channel:///auction-channel/create")
                .as(ChannelAuction.class);

    Assert.assertNotNull(channelCreate);

    boolean result = createUser(channelCreate, "Spock", "passwd");
    Assert.assertTrue(result);

    String id = createAuction(channelCreate, "book", 15);
    Assert.assertNotNull(id);

    AuctionDataPublic data = getAuctionDataPublic(channelCreate, id);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getTitle(), "book");
    Assert.assertNull(data.getLastBid());
  }

  */
/**
   * find an auction
   *//*

  @Test
  public void auctionFind()
  {
    // create the auction by User Spock
    ChannelAuction channelCreate
      = _auctionPod.lookup("channel:///auction-channel/create")
                .as(ChannelAuction.class);

    boolean result = createUser(channelCreate, "Spock", "passwd");
    Assert.assertTrue(result);

    String id = createAuction(channelCreate, "book", 15);
    Assert.assertNotNull(id);

    // find the auction by User Kirk
    ChannelAuction channelFind
      = _auctionPod.lookup("channel:///auction-channel/find")
                .as(ChannelAuction.class);

    result = createUser(channelFind, "Kirk", "passwd");
    Assert.assertTrue(result);

    String idFind = findAuction(channelFind, "book");
    Assert.assertNotNull(idFind);

    AuctionDataPublic data = getAuctionDataPublic(channelCreate, idFind);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getTitle(), "book");
    Assert.assertNull(data.getLastBid());
  }

  */
/**
   * bid on an auction
   *//*

  @Test
  public void auctionBid()
  {
    // create the auction by User Spock
    ChannelAuction channelCreate = createUser("Spock", "password");

    String id = createAuction(channelCreate, "book", 15);
    Assert.assertNotNull(id);

    // bid on the auction by User Kirk
    ChannelAuction channelBid = createUser("Kirk", "password");

    String idBid = findAuction(channelBid, "book");
    Assert.assertNotNull(idBid);

    boolean result = bid(channelBid, idBid, 17);
    Assert.assertTrue(result);

    AuctionDataPublic data = getAuctionDataPublic(channelBid, idBid);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getLastBid().getBid(), new BigDecimal(17));
  }

  private ChannelAuction createUser(String user, String password)
  {
    ChannelAuction channel
      = _auctionPod.lookup("channel:///auction-channel/" + user + "-test-channel")
                .as(ChannelAuction.class);

    boolean result = createUser(channel, user, password);
    Assert.assertTrue(result);

    return channel;
  }

  */
/**
   * listener from auction bid.
   *
   * @throws InterruptedException
   *//*

  @Test
  public void auctionBidListener() throws InterruptedException
  {
    // create the auction by User Spock
    ChannelAuction channelCreate = createUser("Spock", "password");

    String id = createAuction(channelCreate, "book-listener", 15);
    Assert.assertNotNull(id);

    TestChannelListener listenerCreate = new TestChannelListener();
    boolean result = setListener(channelCreate, listenerCreate);
    Assert.assertTrue(result);

    result = addAuctionListener(channelCreate, id);
    Assert.assertTrue(result);

    // bid on the auction by User Kirk
    ChannelAuction channelBid = createUser("Kirk", "password");

    String idBid = findAuction(channelBid, "book-listener");
    Assert.assertNotNull(idBid);

    // check callback state before bid
    Assert.assertEquals("", listenerCreate.getAndClear());

    result = bid(channelBid, idBid, 17);
    Assert.assertTrue(result);

    AuctionDataPublic data = getAuctionDataPublic(channelBid, idBid);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getLastBid().getBid(), new BigDecimal(17));

    // sleep because events are async
    Thread.sleep(100);

    Assert.assertEquals("auction-update auction=book-listener user=Kirk bid=17",
                        listenerCreate.getAndClear());
  }

  */
/**
   * listener from auction bid.
   *
   * @throws InterruptedException
   *//*

  @Test
  public void auctionCompleteListener() throws InterruptedException
  {
    // create the auction by User Spock
    ChannelAuction channelCreate = createUser("Spock2", "password");

    String id = createAuction(channelCreate, "book-close", 15);
    Assert.assertNotNull(id);

    TestChannelListener listenerCreate = new TestChannelListener();
    boolean result = setListener(channelCreate, listenerCreate);
    Assert.assertTrue(result);

    result = addAuctionListener(channelCreate, id);
    Assert.assertTrue(result);

    // bid on the auction by User Kirk
    ChannelAuction channelBid = createUser("Kirk", "password");

    String idBid = findAuction(channelBid, "book-close");
    Assert.assertNotNull(idBid);

    // check callback state before bid
    Assert.assertEquals("", listenerCreate.getAndClear());

    result = bid(channelBid, idBid, 17);
    Assert.assertTrue(result);

    AuctionDataPublic data = getAuctionDataPublic(channelBid, idBid);
    Assert.assertNotNull(data);
    Assert.assertEquals(data.getLastBid().getBid(), new BigDecimal(17));

    // sleep because events are async
    Thread.sleep(100);

    Assert.assertEquals("auction-update auction=book-close user=Kirk bid=17",
                        listenerCreate.getAndClear());

    _testContext.addTime(4, TimeUnit.HOURS);
    Thread.sleep(100);

    Assert.assertEquals("", listenerCreate.getAndClear());

    _testContext.addTime(2, TimeUnit.HOURS);
    Thread.sleep(100);

    Assert.assertEquals("auction-close auction=book-close user=Kirk bid=17",
                        listenerCreate.getAndClear());
  }

  private class TestChannelListener implements ChannelListener
  {
    private String _msg = "";

    @Override
    public void onAuctionUpdate(AuctionDataPublic data)
    {
      if (!"".equals(_msg)) {
        _msg += "\n";
      }

      Auction.Bid bid = data.getLastBid();

      if (bid != null) {
        UserDataPublic userData = getUserDataPublic(bid.getUser());

        _msg += "auction-update auction="
                + data.getTitle()
                + " user="
                + userData.getName()
                + " bid="
                + bid.getBid();
      }
      else {
        _msg += "auction-update auction=" + data.getTitle() + " " + bid;
      }
    }

    @Override
    public void onAuctionClose(AuctionDataPublic data)
    {
      if (!"".equals(_msg)) {
        _msg += "\n";
      }

      Auction.Bid bid = data.getLastBid();

      if (bid != null) {
        UserDataPublic userData = getUserDataPublic(bid.getUser());

        _msg += "auction-close auction=" + data.getTitle() + " user=" + userData
          .getName() + " bid=" + bid.getBid();
      }
      else {
        _msg += "auction-close auction=" + data.getTitle() + " " + bid;
      }
    }

    public String getAuctionData()
    {
      return _msg;
    }

    public void clear()
    {
      _msg = "";
    }

    public String getAndClear()
    {
      String msg = _msg;
      _msg = "";

      return msg;
    }
  }
*/
}
