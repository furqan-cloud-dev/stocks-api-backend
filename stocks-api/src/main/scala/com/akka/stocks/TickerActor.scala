package com.akka.stocks

import akka.actor.typed.{ActorRef, Behavior, PostStop, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import com.akka.stocks.StocksActor.TickerActorDataResponse
import com.akka.stocks.TickerAggregatesBarsAPIActor.GetData
import com.akka.stocks.models.{ClientRequest, Ticker, TickerResults}

object TickerActor {
  sealed trait Command
  final case class GetTickerData(request: ClientRequest, ticker: Ticker, replyTo: ActorRef[TickerActorDataResponse]) extends Command

  sealed trait Events extends Command
  final case class TickerDataResponse(results: TickerResults) extends Events

  def apply() = behaviour()

  private def behaviour(): Behavior[Command] = Behaviors.setup { context =>

    var request : ClientRequest = null
    var replyTo: ActorRef[TickerActorDataResponse] = null

    Behaviors.supervise(TickerAggregatesBarsAPIActor())
      .onFailure[IllegalStateException](SupervisorStrategy.restart.withStopChildren(false))

    Behaviors.receiveMessage[Command] {
      case GetTickerData(requestObj,ticker, replyToActorRef) =>
        request = requestObj
        replyTo = replyToActorRef
        val tickerAggregatesBarsAPIActor = context.spawn(TickerAggregatesBarsAPIActor(), s"TickerAggregatesBarsAPIActor-${java.util.UUID.randomUUID.toString}")
        tickerAggregatesBarsAPIActor ! GetData(ticker,context.self)
        Behaviors.same


      case TickerDataResponse(tickerResults) =>
        replyTo ! TickerActorDataResponse(request,tickerResults)
        Behaviors.same


    }.receiveSignal {
      case(_, PostStop) =>
        println(s"stopping actor")
        request = null
        replyTo = null
        Behaviors.same
    }
  }


}
