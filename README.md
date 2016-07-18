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

* AuctionSession, AuctionUserSession, AuctionAdminSession
* User
* UserVault
* Auction
* AuctionVault
* WebAuctionUpdates

HTML, JavaScript:
* index.html

##### The main classes are

Java:

* AbstractAuctionSession, AuctionUserSessionImpl - implements a user session; invoked by the UI (index.html)
* UserImpl - implements User; manages UserDataPublic class which contains user detail
* UserVault - creates and manages users
* AuctionImpl - implements Auction; manages AuctionDataPublic class which contains Auction detail
* AuctionVault - creates and manages auctions

##### Building the Auction application

##### Running the Auction application

execute `gradle clean jar run`

For additional documentaton on Baratine™ visit [Baratine Home]
[Baratine Home]: http://baratine.io


