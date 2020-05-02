package com.knoldus.credibility

import org.scalatest.wordspec.AnyWordSpec

class CredibilityInformationTest extends AnyWordSpec {

  "currentTotal" should {
    "return zero if there are no transactions" in {
      val credibility = CredibilityInformation.empty

      assert(credibility.currentTotal === 0)
    }
    "return the value of an credit if it is the only transaction" in {
      val credibility = CredibilityInformation(Seq(Credit(10)))

      assert(credibility.currentTotal === 10)
    }
    "return a negative value of a debit if it is the only transaction" in {
      val credibility = CredibilityInformation(Seq(Debit(10)))

      assert(credibility.currentTotal === -10)
    }
    "return the combined value of all transactions" in {
      val credibility = CredibilityInformation(Seq(
        Credit(100),
        Debit(50),
        Credit(30),
        Debit(20)
      ))

      assert(credibility.currentTotal === 60)
    }
  }

  "applyAdjustment" should {
    "Add the adjustment to the list" in {
      val credibility = CredibilityInformation.empty.applyAdjustment(Credit(10))

      assert(credibility.adjustments === Seq(Credit(10)))
    }
    "Apply multiple adjustments" in {
      val credibility = CredibilityInformation.empty
        .applyAdjustment(Credit(10))
        .applyAdjustment(Debit(20))
        .applyAdjustment(Credit(30))

      assert(credibility.adjustments === Seq(Credit(10), Debit(20), Credit(30)))
    }
  }

}
