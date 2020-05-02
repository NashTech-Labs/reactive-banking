package com.knoldus.credibility

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import com.knoldus.credibility.CredibilityActor.CredibilityAdjustmentRejected
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future

class CredibilityActorTest
  extends AnyWordSpec
    with AkkaSpec
    with ScalaFutures {

  class BrokenCredibilityRepository extends CredibilityRepository {
    override def updateCredibility(
                                    credibilityId: CredibilityId,
                                    credibilityInformation: CredibilityInformation): Future[Done] = {
      Future.failed(new Exception("Boom"))
    }

    override def findCredibility(credibilityId: CredibilityId): Future
      [CredibilityInformation] = Future.failed(new Exception("Bam"))
  }

  class TestContext {
    val credibilityId = CredibilityId(UUID.randomUUID.toString)
    val credibilityRepository = new InMemoryCredibilityRepository
    val brokenRepository = new BrokenCredibilityRepository

    lazy val credibilityActor: ActorRef = system.actorOf(
      CredibilityActor.props(credibilityRepository),
      credibilityId.value
    )
  }

  "The Actor" should {

    "Load it's state from the repo on startup" in new TestContext {
      val state = CredibilityInformation(Seq(Credit(10)))
      credibilityRepository.updateCredibility(credibilityId, state)

      val result = (credibilityActor ? CredibilityActor.GetCredibilityInformation())
        .mapTo[CredibilityInformation]

      assert(result.futureValue === state)
    }
  }

  "ApplyCredibilityAdjustment" should {

    "Return a corresponding event" in new TestContext {
      val result = (credibilityActor ? CredibilityActor.ApplyCredibilityAdjustment(Credit(10)))
        .mapTo[CredibilityActor.CredibilityAdjustmentApplied]

      assert(result.futureValue === CredibilityActor.CredibilityAdjustmentApplied(Credit(10)))
    }

    "Apply all Credibility Adjustments" in new TestContext {
      credibilityActor ! CredibilityActor.ApplyCredibilityAdjustment(Credit(10))
      credibilityActor ! CredibilityActor.ApplyCredibilityAdjustment(Debit(5))
      credibilityActor ! CredibilityActor.ApplyCredibilityAdjustment(Credit(20))

      val result = (credibilityActor ? CredibilityActor.GetCredibilityInformation())
        .mapTo[CredibilityInformation]

      assert(result.futureValue === CredibilityInformation(Seq(
        Credit(10),
        Debit(5),
        Credit(20)
      )))
    }

    "Update the Credibility Repository" in new TestContext {
      (credibilityActor ? CredibilityActor.ApplyCredibilityAdjustment(Credit(10)))
        .mapTo[CredibilityActor.CredibilityAdjustmentApplied].futureValue

      assert(credibilityRepository.findCredibility(credibilityId).futureValue === CredibilityInformation(Seq(Credit(10))))
    }

    "Fail to debit if there is insufficient money" in new TestContext {
      (credibilityActor ? CredibilityActor.ApplyCredibilityAdjustment(Credit(10)))
        .mapTo[CredibilityActor.CredibilityAdjustmentApplied].futureValue

      val result = (credibilityActor ? CredibilityActor.ApplyCredibilityAdjustment(Debit(15)))
        .mapTo[CredibilityActor.CredibilityAdjustmentRejected].futureValue

      assert(result === CredibilityAdjustmentRejected(Debit(15), "Insufficient Money"))
    }

    "Fail if it can't write to the repo" in new TestContext {
      override lazy val credibilityActor = system.actorOf(
        CredibilityActor.props(brokenRepository)
      )

      val result = (credibilityActor ? CredibilityActor.ApplyCredibilityAdjustment(Credit(10)))
        .mapTo[CredibilityActor.CredibilityAdjustmentApplied]

      intercept[Exception] {
        result.futureValue
      }
    }
  }

  "GetCredibilityInformation" should {

    "return empty CredibilityInformation if no adjustments have been applied" in new TestContext{

      val result = (credibilityActor ? CredibilityActor.GetCredibilityInformation())
        .mapTo[CredibilityInformation]

      assert(result.futureValue === CredibilityInformation.empty)
    }

    "return the updated CredibilityInformation if an adjustment has been applied" in new TestContext {
      credibilityActor ! CredibilityActor.ApplyCredibilityAdjustment(Credit(10))

      val result = (credibilityActor ? CredibilityActor.GetCredibilityInformation())
        .mapTo[CredibilityInformation]

      assert(result.futureValue === CredibilityInformation(Seq(Credit(10))))
    }
  }
}
