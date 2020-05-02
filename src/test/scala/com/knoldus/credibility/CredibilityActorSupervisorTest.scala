package com.knoldus.credibility

import akka.pattern.ask
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec

class CredibilityActorSupervisorTest
  extends AnyWordSpec
    with AkkaSpec
    with ScalaFutures {

  class TestContext {
    val credibilityRepository = new InMemoryCredibilityRepository
    val supervisor = system.actorOf(CredibilityActorSupervisor.props(credibilityRepository))
  }

  "Deliver" should {
    "create an actor and send it the command" in new TestContext {
      val id = CredibilityId("Id")

      supervisor ! CredibilityActorSupervisor.Deliver(
        CredibilityActor.ApplyCredibilityAdjustment(Credit(10)),
        id
      )

      val result = (supervisor ? CredibilityActorSupervisor.Deliver(
        CredibilityActor.GetCredibilityInformation(),
        id
      )).mapTo[CredibilityInformation]

      assert(result.futureValue === CredibilityInformation(Seq(Credit(10))))
    }
    "reuse existing actors" in new TestContext {
      val id = CredibilityId("Id")

      supervisor ! CredibilityActorSupervisor.Deliver(
        CredibilityActor.ApplyCredibilityAdjustment(Credit(10)),
        id
      )

      supervisor ! CredibilityActorSupervisor.Deliver(
        CredibilityActor.ApplyCredibilityAdjustment(Debit(5)),
        id
      )

      val result = (supervisor ? CredibilityActorSupervisor.Deliver(
        CredibilityActor.GetCredibilityInformation(),
        id
      )).mapTo[CredibilityInformation]

      assert(result.futureValue === CredibilityInformation(Seq(Credit(10), Debit(5))))
    }
  }
}
