package com.akka.stocks

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import com.akka.stocks.StockAPIActor.GetAllTickers
import com.akka.stocks.TickerActor.GetTickerData
import com.akka.stocks.models.{ClientRequest, Stock, Ticker, TickerResults}

import scala.collection.mutable


case class Stocks(results: Seq[Stock])

object StocksActor {
  sealed trait Command
  final case class GetStocks(request: ClientRequest, replyTo: ActorRef[Stocks]) extends Command
  final case class GetTickerUpdates(request: ClientRequest, ticker: Ticker, replyTo: ActorRef[TickerResults]) extends Command

  sealed trait Events extends Command
  final case class StocksDataResponse(request: ClientRequest, stocks: Stocks) extends Events
  final case class TickerActorDataResponse(request: ClientRequest, tickerResults: TickerResults) extends Events

  def apply(): Behavior[Command] = behaviour()

  private def behaviour(): Behavior[Command] = Behaviors.setup { context =>
    val stocksRequests: mutable.Map[String, ActorRef[Stocks]] = mutable.Map()
    val tickerDataRequests: mutable.Map[String, ActorRef[TickerResults]] = mutable.Map()


    Behaviors.supervise(StockAPIActor())
      .onFailure[IllegalStateException](SupervisorStrategy.restart.withStopChildren(false))

    Behaviors.receiveMessage {
      case GetStocks(request, replyTo) =>
        stocksRequests(request.apiKey) = replyTo
        val stockAPIActor = context.spawn(StockAPIActor(), s"StockAPIActor-${java.util.UUID.randomUUID.toString}")
        stockAPIActor ! GetAllTickers(request,context.self)
        Behaviors.same


      case GetTickerUpdates(request,ticker,replyTo) =>
        tickerDataRequests(request.apiKey) = replyTo
        val tickerActor = context.spawn(TickerActor(), s"TickerActor-${java.util.UUID.randomUUID.toString}")
        tickerActor ! GetTickerData(request,ticker,context.self)
        Behaviors.same


      case StocksDataResponse(request, stocks) =>
        val actorRef = stocksRequests(request.apiKey)
        actorRef ! stocks
        stocksRequests.remove(request.apiKey)
        Behaviors.same


      case TickerActorDataResponse(request, tickerResults) =>
        val actorRef = tickerDataRequests(request.apiKey)
        actorRef ! tickerResults
        tickerDataRequests.remove(request.apiKey)
        Behaviors.same
    }
  }


}
