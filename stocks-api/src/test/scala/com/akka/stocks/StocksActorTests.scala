package com.akka.stocks

import scala.language.postfixOps

class StocksActorTests extends TestService {
  import scala.concurrent.duration._
  val sender = testKit.spawn(StocksActor(), "StocksActor")
  val probe = testKit.createTestProbe[Stocks]()

  val clientRequest = com.akka.stocks.models.ClientRequest("apiKey")
  sender ! StocksActor.GetStocks(clientRequest, probe.ref)

  probe.expectMessageType[Stocks]
  probe.expectNoMessage(1000 millis)
}
