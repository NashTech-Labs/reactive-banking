package com.knoldus.credibility

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import com.knoldus.credibility.CredibilityActorSupervisor.Deliver
import org.scalatest.wordspec.AnyWordSpec

class CredibilityRoutesTest
  extends AnyWordSpec with ScalatestRouteTest {

  val credibilityActorSupervisor = TestProbe()
  val routes = new CredibilityRoutes(credibilityActorSupervisor.ref)

  private def setAutoPilot(message: Any, response: Any): Unit = {
    credibilityActorSupervisor.setAutoPilot(
      (sender, msg) => {
        msg match {
          case `message` =>
            sender ! response
            TestActor.KeepRunning
        }
      }
    )
  }

  "/credibility/{id}" should {
    "Return the results from the supervisor" in {
      val credibilityId = CredibilityId("someId")
      val expectedRequest = Deliver(
        CredibilityActor.GetCredibilityInformation(),
        credibilityId
      )
      val expectedResponse = s"""Current Balance: 10\nHistory:\n- Credit(10)"""

      setAutoPilot(expectedRequest, CredibilityInformation(Seq(Credit(10))))

      Get(s"/credibility/${credibilityId.value}") ~> routes.routes ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`text/plain(UTF-8)`)
        assert(entityAs[String] === expectedResponse)
      }
    }
  }

  "/credibility/{id}/credit/{money}" should {
    "Indicate the adjustment was applied if it succeeds." in {
      val credibilityId = CredibilityId("someId")
      val money = 30
      val expectedRequest = Deliver(
        CredibilityActor.ApplyCredibilityAdjustment(Credit(money)),
        credibilityId
      )
      val expectedResponse = CredibilityActor.CredibilityAdjustmentApplied(Credit(money))

      setAutoPilot(expectedRequest, expectedResponse)

      Post(s"/credibility/${credibilityId.value}/credit/$money") ~> routes.routes ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`text/plain(UTF-8)`)
        assert(entityAs[String] === s"Applied: ${expectedResponse.adjustment}")
      }
    }
    "Indicate the adjustment was not applied if it failed." in {
      val credibilityId = CredibilityId("someId")
      val money = 30
      val expectedRequest = Deliver(
        CredibilityActor.ApplyCredibilityAdjustment(Credit(money)),
        credibilityId
      )
      val expectedResponse = CredibilityActor.CredibilityAdjustmentRejected(Credit(money), "reason")

      setAutoPilot(expectedRequest, expectedResponse)

      Post(s"/credibility/${credibilityId.value}/credit/$money") ~> routes.routes ~> check {
        assert(status === StatusCodes.BadRequest)
        assert(contentType === ContentTypes.`text/plain(UTF-8)`)
        assert(entityAs[String] === s"Rejected: ${expectedResponse.reason}")
      }
    }
  }

  "/credibility/{id}/debit/{money}" should {
    "Indicate the adjustment was applied if it succeeds." in {
      val credibilityId = CredibilityId("someId")
      val money = 30
      val expectedRequest = Deliver(
        CredibilityActor.ApplyCredibilityAdjustment(Debit(money)),
        credibilityId
      )
      val expectedResponse = CredibilityActor.CredibilityAdjustmentApplied(Debit(money))

      setAutoPilot(expectedRequest, expectedResponse)

      Post(s"/credibility/${credibilityId.value}/debit/$money") ~> routes.routes ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`text/plain(UTF-8)`)
        assert(entityAs[String] === s"Applied: ${expectedResponse.adjustment}")
      }
    }
    "Indicate the adjustment was rejected if it fails." in {
      val credibilityId = CredibilityId("someId")
      val money = 30
      val expectedRequest = Deliver(
        CredibilityActor.ApplyCredibilityAdjustment(Debit(money)),
        credibilityId
      )
      val expectedResponse = CredibilityActor.CredibilityAdjustmentRejected(Debit(money), "reason")

      setAutoPilot(expectedRequest, expectedResponse)

      Post(s"/credibility/${credibilityId.value}/debit/$money") ~> routes.routes ~> check {
        assert(status === StatusCodes.BadRequest)
        assert(contentType === ContentTypes.`text/plain(UTF-8)`)
        assert(entityAs[String] === s"Rejected: ${expectedResponse.reason}")
      }
    }
  }

}
