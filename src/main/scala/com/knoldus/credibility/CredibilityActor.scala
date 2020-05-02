package com.knoldus.credibility

import akka.actor.{Actor, ActorLogging, Props, Stash}
import akka.pattern.pipe

object CredibilityActor {
  sealed trait Command extends SerializableMessage
  sealed trait Event extends SerializableMessage

  case class ApplyCredibilityAdjustment(adjustment: CredibilityAdjustment) extends Command
  case class CredibilityAdjustmentApplied(adjustment: CredibilityAdjustment) extends Event
  case class CredibilityAdjustmentRejected(adjustment: CredibilityAdjustment, reason: String) extends Event

  case class GetCredibilityInformation() extends Command

  def props(credibilityRepository: CredibilityRepository): Props =
    Props(new CredibilityActor(credibilityRepository))
}

class CredibilityActor(credibilityRepository: CredibilityRepository)
  extends Actor
    with Stash
    with ActorLogging {
  import CredibilityActor._
  import context.dispatcher

  private val credibilityId = CredibilityId(self.path.name)
  private var credibilityInformation = CredibilityInformation.empty

  override def preStart(): Unit = {
    super.preStart()

    credibilityRepository.findCredibility(credibilityId).map {
      credibilityInfo =>
        log.info(s"Credibility Information Loaded For ${credibilityId.value}")
        credibilityInfo
    }.recover {
      case _ =>
        log.info(s"Creating New Credibility Account For ${credibilityId.value}.")
        CredibilityInformation.empty
    }.pipeTo(self)
  }

  override def receive: Receive = initializing

  private def initializing: Receive = {
    case credibilityInfo: CredibilityInformation =>
      credibilityInformation = credibilityInfo
      context.become(running)
      unstashAll()
    case _ =>
      stash()
  }

  private def running: Receive = {
    case cmd: ApplyCredibilityAdjustment => handle(cmd)
    case GetCredibilityInformation() =>
      log.info(s"Retrieving Credibility Information For ${credibilityId.value}")
      sender() ! credibilityInformation
  }

  private def handle(cmd: ApplyCredibilityAdjustment) = {
    cmd match {
      case ApplyCredibilityAdjustment(Debit(money)) if money > credibilityInformation.currentTotal =>
        log.info(s"Insufficient Money For ${credibilityId.value}")
        sender() ! CredibilityAdjustmentRejected(Debit(money), "Insufficient Money")
      case ApplyCredibilityAdjustment(adjustment) =>
        log.info(s"Applying $adjustment for ${credibilityId.value}")
        credibilityInformation = credibilityInformation.applyAdjustment(adjustment)
        credibilityRepository.updateCredibility(credibilityId, credibilityInformation).map { _ =>
          CredibilityAdjustmentApplied(adjustment)
        }.pipeTo(sender())
    }
  }
}
