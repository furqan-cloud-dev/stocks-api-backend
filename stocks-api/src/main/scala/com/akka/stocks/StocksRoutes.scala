package com.akka.stocks

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{_symbol2NR, complete, concat, get, onSuccess, parameters, pathEnd, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.akka.stocks.StocksActor.{GetStocks, GetTickerUpdates}

import scala.concurrent.Future
import JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import com.akka.stocks.models.{ClientRequest, Ticker, TickerResults}


class StocksRoutes(stocksActor: ActorRef[StocksActor.Command])(implicit val system: ActorSystem[_]) extends appJSONProtocol with SprayJsonSupport {

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getStocks(clientRequest: ClientRequest): Future[Stocks] =
    stocksActor.ask(GetStocks(clientRequest, _))

  def getTicker(clientRequest: ClientRequest, ticker: Ticker): Future[TickerResults] =
    stocksActor.ask(GetTickerUpdates(clientRequest,ticker, _))




  val stocksRoutes: Route =
    pathPrefix("stocks") {
      concat(
        pathEnd {
          concat(
            get {
              parameters(Symbol("apiKey").as[String]) { apiKey =>
                onSuccess(getStocks(ClientRequest(apiKey))) { stocks =>
                  complete(StatusCodes.OK,stocks)
                }
              }
            })
        })
    }


  val tickerRoutes: Route =
    pathPrefix("ticker") {
      concat(
        pathEnd {
          concat(
            get {
              parameters(Symbol("apiKey").as[String], Symbol("symbol").as[String]) { (apiKey, symbol) =>
                onSuccess(getTicker(ClientRequest(apiKey), Ticker(symbol))) { results =>
                  complete(StatusCodes.OK,results)
                }
              }
            })
        })
    }




  /*
    val stocksRoutesPost: Route =
      pathPrefix("stocks") {
        Directives.concat(
          pathEnd {
            concat(
              post {
                entity(as[AuthenticatedRequest]) { request =>
                  onSuccess(getStocks(request)) { stocks =>
                    val entity = HttpEntity(ContentTypes.`application/json`, stocks.results.map(c => c.toJson).mkString("[", ",", "]"))
                    complete((StatusCodes.OK, entity))
                  }
                }
              })
          },
        )
      }

   */


}
