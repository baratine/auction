### Baratine™ Auction

***

##### Auction application demonstrates how to use Baratine™ to build a Single Page Web Application.

The application uses the following Baratine provided services
 * DatabaseService
 * Store (key - value)
 * Timer
 * Event
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

##### Building the Auction application

* install Baratine Maven Collection [Baratine Maven Collection] 

##### Running the Auction application

* install Baratine™ version 0.8.8 or better
* install maven plugins for baratine: https://github.com/baratine/maven-collection-baratine
* set BARATINE_HOME to point to installation directory or create a link from ~/baratine
* cd to auction directory and run src/main/bin/run-auction.sh script
* navigate to src/main/web directory and open auction.html file in a browser that supports WebSockets

For additional documentaton on Baratine™ visit [Baratine Home]
[Baratine Home]: http://baratine.io
[Baratine Maven Collection]: https://github.com/baratine/maven-collection-baratine

