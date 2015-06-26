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
* index.html

##### The main classes are

Java:

* AuctionSessionImpl - implements a user session; invoked by the UI (index.html)
* UserImpl - implements User; manages UserDataPublic class which contains user detail
* UserManagerImpl - creates and manages users
* AuctionImpl - implements Auction; manages AuctionDataPublic class which contains Auction detail
* AuctionManagerImpl - creates and manages auctions

##### Building the Auction application

* install Baratine Maven Collection [Baratine Maven Collection] 

##### Running the Auction application

1. install maven baratine plugin: https://github.com/baratine/maven-collection-baratine
  1. git clone git@github.com:baratine/maven-collection-baratine.git
  2. change to maven-collection-baratine and run mnv install
2. mvn -Dmaven.test.skip=true -P release package baratine:run
3. open latest browser and navigate to http://localhost:8085

For additional documentaton on Baratine™ visit [Baratine Home]
[Baratine Home]: http://baratine.io
[Baratine Maven Collection]: https://github.com/baratine/maven-collection-baratine

