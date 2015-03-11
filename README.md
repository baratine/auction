## Baratine™ Auction

***

#### Auction application demonstrates how to use Baratine™ to
build a Single Page Web Applicaton.

The application uses the following Baratine provided services
 * Database
 * Timer
 * Events
 * WebSocket

#### The main interfaces are

Java:

* AuctionSession
* User
* UserManager
* Auction
* AuctionManager
* ChannelListener

HTML, JavaScript:
* auction.html

#### The main classes are

Java:

* AuctionSessionImpl - implements a user session; invoked by the UI (auction.html)
* UserImpl - implements a User; manages UserDataPublic class which contains user detail
* UserManagerImpl - creates and manages users
* AuctionImpl - implements an Auction; manages AuctionDataPublic class which contains Auction detail
* AuctionManagerImpl - creates and manages auctions

