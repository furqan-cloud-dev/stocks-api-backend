package com.akka.stocks.models



case class Stock(ticker: String, name: String, market: String, currency_name: String)


/*
{
 "results": [
  {
   "ticker": "A",
   "name": "Agilent Technologies Inc.",
   "market": "stocks",
   "locale": "us",
   "primary_exchange": "XNYS",
   "type": "CS",
   "active": true,
   "currency_name": "usd",
   "cik": "0001090872",
   "composite_figi": "BBG000C2V3D6",
   "share_class_figi": "BBG001SCTQY4",
   "last_updated_utc": "2022-06-19T00:00:00Z"
  },
 */