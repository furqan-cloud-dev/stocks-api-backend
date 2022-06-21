package com.akka.stocks

import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.Timeout
import com.akka.stocks.StocksActor.StocksDataResponse
import com.akka.stocks.models.ClientRequest

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

object StockAPIActor extends appJSONProtocol with SprayJsonSupport {
  sealed trait Command
  final case class GetAllTickers(request: ClientRequest, replyTo: ActorRef[StocksDataResponse]) extends Command

  def apply() = behaviour()

  private def behaviour(): Behavior[Command] = Behaviors.setup { context =>

    implicit val timeout: Timeout = Timeout.create(context.system.settings.config.getDuration("my-app.routes.ask-timeout"))
    implicit val scheduler: Scheduler = context.system.scheduler
    implicit val system = context.system


    Behaviors.receiveMessage {
      case GetAllTickers(request, replyTo) =>
        // Query all ticker symbols which are supported by Polygon.io. This API currently includes Stocks/Equities, Crypto, and Forex.
        val getRequest = Get("https://api.polygon.io/v3/reference/tickers?active=true&sort=ticker&order=asc&limit=10&apiKey=vB1Owp_saOr5wHEJgwrNchzBeX3pmWNa")
        val responseFuture = Http().singleRequest(getRequest)
        responseFuture
          .onComplete {
            case Success(res) => {
              val future: Future[Stocks] = Unmarshal(res).to[Stocks]
                future.map( stocks => {
                  replyTo ! StocksDataResponse(request,stocks)
                })
            }
            case Failure(_)   => replyTo ! StocksDataResponse(request,Stocks(results = Seq.empty))
          }

        Behaviors.same
    }
  }


}
