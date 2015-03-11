### Baratine™ Auction

***

##### Auction application demonstrates how to use Baratine™ to build a Single Page Web Application.

The application uses the following Baratine provided services
 * Database
 * Timer
 * Events
 * WebSocket

##### The main interfaces are

Java:

* AuctionSession
* User
* UserManager
* Auction
* AuctionManager
* ChannelListener

HTML, JavaScript:
* auction.html

##### The main classes are

Java:

* AuctionSessionImpl - implements a user session; invoked by the UI (auction.html)
* UserImpl - implements User; manages UserDataPublic class which contains user detail
* UserManagerImpl - creates and manages users
* AuctionImpl - implements Auction; manages AuctionDataPublic class which contains Auction detail
* AuctionManagerImpl - creates and manages auctions

##### Running the Auction application

- install Baratine™ version 0.8.8 or better
– set BARATINE_HOME to point to installation directory (or create a link from ~/baratine)
– cd to auction directory and run src/main/bin/run-auction.sh script
- navigate to src/web directory and open auction.html file in a browser that supports WebSockets

For additional information see [Baratine]
[Baratine]: https://baratine.io

