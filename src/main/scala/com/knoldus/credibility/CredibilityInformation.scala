package com.knoldus.credibility

case class CredibilityId(value: String)

trait CredibilityAdjustment extends SerializableMessage {
  def absoluteValue: Int
}

case class Credit(money:Int) extends CredibilityAdjustment {
  require(money > 0, "money must be a positive integer.")
  override def absoluteValue: Int = money
}
case class Debit(money:Int) extends CredibilityAdjustment {
  require(money > 0, "money must be a positive integer.")
  override def absoluteValue: Int = -money
}

object CredibilityInformation {
  val empty = CredibilityInformation(Seq.empty)
}

case class CredibilityInformation(adjustments: Seq[CredibilityAdjustment]) extends SerializableMessage {
  val currentTotal:Int = adjustments.foldLeft(0) {
    case (sum, adj) => sum + adj.absoluteValue
  }

  def applyAdjustment(adjustment: CredibilityAdjustment): CredibilityInformation = {
    this.copy(adjustments = adjustments :+ adjustment)
  }
}
