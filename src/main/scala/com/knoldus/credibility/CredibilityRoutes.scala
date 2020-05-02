package com.knoldus.credibility

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.knoldus.credibility.CredibilityActor.{CredibilityAdjustmentApplied, CredibilityAdjustmentRejected}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class CredibilityRoutes(credibilityActors: ActorRef)(implicit ec: ExecutionContext) {
  private implicit val timeout: Timeout = Timeout(5.seconds)

  lazy val routes: Route =
    pathPrefix("credibility") {
      pathPrefix(Segment) { id =>
        path("credit" / IntNumber) { value =>
          post {
            val credibilityId = CredibilityId(id)
            val command = CredibilityActor.ApplyCredibilityAdjustment(Credit(value))
            val result = (credibilityActors ? CredibilityActorSupervisor.Deliver(command, credibilityId))
              .mapTo[CredibilityActor.Event]

            onComplete(result) {
              case Success(CredibilityAdjustmentApplied(adjustment)) =>
                complete(StatusCodes.OK, s"Applied: $adjustment")
              case Success(CredibilityAdjustmentRejected(adjustment, reason)) =>
                complete(StatusCodes.BadRequest, s"Rejected: $reason")
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError, ex.getMessage)
            }
          }
        } ~
        path("debit" / IntNumber) { value =>
          post {
            val credibilityId = CredibilityId(id)
            val command = CredibilityActor.ApplyCredibilityAdjustment(Debit(value))
            val result = (credibilityActors ? CredibilityActorSupervisor.Deliver(command, credibilityId))
              .mapTo[CredibilityActor.Event]

            onComplete(result) {
              case Success(CredibilityAdjustmentApplied(adjustment)) =>
                complete(StatusCodes.OK, s"Applied: $adjustment")
              case Success(CredibilityAdjustmentRejected(adjustment, reason)) =>
                complete(StatusCodes.BadRequest, s"Rejected: $reason")
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError, ex.getMessage)
            }
          }
        } ~
        pathEnd {
          get {
            val credibilityId = CredibilityId(id)
            val command = CredibilityActor.GetCredibilityInformation()
            val result = (credibilityActors ? CredibilityActorSupervisor.Deliver(command, credibilityId))
              .mapTo[CredibilityInformation]
              .map { info =>
                s"Current Balance: ${info.currentTotal}\nHistory:\n" + info.adjustments.mkString("- ", "\n- ", "")
              }

            complete(result)
          }
        }
      }
    }
}
