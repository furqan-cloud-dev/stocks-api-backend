package com.akka.stocks

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.akka.stocks.JsonFormats.appJSONProtocol
import com.akka.stocks.TickerActor.TickerDataResponse
import com.akka.stocks.models.{Ticker, TickerResults}
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


object TickerAggregatesBarsAPIActor extends appJSONProtocol with SprayJsonSupport {
  sealed trait Command
  final case class GetData(ticker: Ticker, replyTo: ActorRef[TickerDataResponse]) extends Command

  def apply() = behaviour()

  private def behaviour(): Behavior[Command] = Behaviors.setup { context =>

//    implicit val timeout: Timeout = Timeout.create(context.system.settings.config.getDuration("my-app.routes.ask-timeout"))
//    implicit val scheduler: Scheduler = context.system.scheduler
    implicit val system = context.system

    Behaviors.receiveMessage {
      case GetData(ticker, replyTo) =>
        // Get aggregate bars for a stock over a given date range in custom time window sizes.
        val url = Constants.polygonBaseUrl + "v2/aggs/ticker/" + ticker.symbol + "/range/1/day/2022-06-01/2022-06-22?adjusted=true&sort=desc&limit=120&apiKey=" + Constants.apiKey
        val request = Get(url)
        val responseFuture = Http().singleRequest(request)
        responseFuture
          .onComplete {
            case Success(res) => {
              val future: Future[TickerResults] = Unmarshal(res).to[TickerResults]
              future.map( tickerResults => {
                replyTo ! TickerDataResponse(tickerResults)
              })
            }
            case Failure(_)   => replyTo ! TickerDataResponse(TickerResults(results = Seq.empty))
          }

        Behaviors.same
    }
  }


}
