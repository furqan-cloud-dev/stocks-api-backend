package com.akka.stocks

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestService
  extends AnyWordSpec
    with BeforeAndAfterAll
    with Matchers {
  val testKit = ActorTestKit()
  implicit val system = testKit.system
  override def afterAll(): Unit = testKit.shutdownTestKit()
}