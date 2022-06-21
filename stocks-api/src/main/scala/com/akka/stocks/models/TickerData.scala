package com.akka.stocks.models


case class Ticker(symbol: String)
//case class TickerData(closePrice: Double, highestPrice: Double, lowestPrice: Double)
case class TickerData(c: Double, h: Double, l: Double)

case class TickerResults(results: Seq[TickerData])
