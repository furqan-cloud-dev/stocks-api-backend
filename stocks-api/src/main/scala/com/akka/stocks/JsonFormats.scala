package com.akka.stocks

import com.akka.stocks.models.{ClientRequest, Stock, Ticker, TickerData, TickerResults}
import spray.json.DefaultJsonProtocol.jsonFormat3
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val authenticatedRequestJsonFormat: RootJsonFormat[ClientRequest] = jsonFormat1(ClientRequest)

  trait appJSONProtocol extends DefaultJsonProtocol {
    implicit val stockJsonFormat: RootJsonFormat[Stock] = jsonFormat4(Stock)
    implicit val stocksJsonFormat: RootJsonFormat[Stocks] = jsonFormat1(Stocks)

    implicit val tickerDataJsonFormat: RootJsonFormat[TickerData] = jsonFormat3(TickerData)
    implicit val tickerJsonFormat: RootJsonFormat[Ticker] = jsonFormat1(Ticker)
    implicit val tickerResultsJsonFormat: RootJsonFormat[TickerResults] = jsonFormat1(TickerResults)

  }
}
