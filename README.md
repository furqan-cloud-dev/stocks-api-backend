# stocks-api-backend
Stocks API Backend provides REST endpoints that let you query the latest market data from all US stock exchanges. You can also find data on company financials, stock market holidays, corporate actions, and more

- Using Akka HTTP server
- SBT Build Tools
- IntelliJ IDEA CE
- Akka Actors to implement asynchronous and non-blocking system


Download the project
- Terminal : sbt run
[] - Server online at http://127.0.0.1:8080/

Acess the following routes to get the data:
Stocks:
http://localhost:8080/stocks?apiKey=123

Ticker:
http://localhost:8080/ticker?apiKey=123&symbol=A