package com.knoldus.credibility

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}

object CredibilityActorSupervisor {
  case class Deliver(command: CredibilityActor.Command, to: CredibilityId) extends SerializableMessage

  def props(credibilityRepository: CredibilityRepository): Props =
    Props(new CredibilityActorSupervisor(credibilityRepository))

  val idExtractor: ExtractEntityId = {
    case Deliver(msg, id) => (id.value.toString, msg)
  }

  val shardIdExtractor: ExtractShardId = {
    case Deliver(_, id) =>
      (Math.abs(id.value.hashCode) % 30).toString
  }
}

class CredibilityActorSupervisor(credibilityRepository: CredibilityRepository) extends Actor {
  import CredibilityActorSupervisor._

  protected def createCredibilityActor(name: String): ActorRef = {
    context.actorOf(CredibilityActor.props(credibilityRepository), name)
  }

  override def receive: Receive = {
    case Deliver(command, to) =>
      val credibilityActor = context.child(to.value)
        .getOrElse(createCredibilityActor(to.value))

      credibilityActor.forward(command)
  }
}
