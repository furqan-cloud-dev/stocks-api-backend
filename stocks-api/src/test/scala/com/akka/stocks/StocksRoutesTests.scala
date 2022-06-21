package com.akka.stocks

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.MissingQueryParamRejection
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.DurationInt

class StocksRoutesTests extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  implicit def default(implicit system: akka.actor.ActorSystem) = RouteTestTimeout(new DurationInt(5).second.dilated(system))


  val stocksActor = testKit.spawn(StocksActor())
  lazy val routes = new StocksRoutes(stocksActor).stocksRoutes

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  "StocksRoutes" should {
    "Request is missing required query parameter 'apiKey': (GET /stocks)" in {
      val request = HttpRequest(uri = "/stocks")
      request ~> routes ~> check {
        rejection should equal (MissingQueryParamRejection("apiKey"))
      }
    }


    "200 OK json response with valid 'apiKey' query parameter': (GET /stocks?apiKey=123)" in {
      val request = HttpRequest(uri = "/stocks?apiKey=123")
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)
      }
    }


  }


}
