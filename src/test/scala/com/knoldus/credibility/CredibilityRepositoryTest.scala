package com.knoldus.credibility

import java.nio.file.{Files, Path}
import java.util.UUID

import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext.Implicits.global

class InMemoryCredibilityRepositoryTest extends CredibilityRepositoryTest {
  override val credibilityRepository: CredibilityRepository =
    new InMemoryCredibilityRepository()
}

class FileBasedCredibilityRepositoryTest
  extends CredibilityRepositoryTest
    with BeforeAndAfterAll {
  val tmpDir: Path = Files.createTempDirectory("filebasedrepotest")
  tmpDir.toFile.deleteOnExit()

  override val credibilityRepository: CredibilityRepository =
    new FileBasedCredibilityRepository(tmpDir)

  override protected def afterAll(): Unit = {
    super.afterAll()
    tmpDir.toFile.listFiles().foreach(_.deleteOnExit())
    tmpDir.toFile.deleteOnExit()
  }
}

trait CredibilityRepositoryTest extends AnyWordSpec with ScalaFutures {
  val credibilityRepository: CredibilityRepository

  def createCredibilityId(): CredibilityId = CredibilityId(UUID.randomUUID.toString)

  "findCredibility" should {
    "return nothing if the id does not exist" in {
      val result = credibilityRepository.findCredibility(createCredibilityId())

      intercept[Exception] {
        result.futureValue
      }
    }
    "return the credibility if it exists" in {
      val info = CredibilityInformation(Seq(Credit(10), Debit(5)))
      val id = createCredibilityId()

      credibilityRepository.updateCredibility(id, info).futureValue

      val result = credibilityRepository.findCredibility(id)

      assert(result.futureValue === info)
    }
    "return the correct credibility if multiples exist" in {
      val info1 = CredibilityInformation(Seq(Credit(10), Debit(5)))
      val id1 = createCredibilityId()

      val info2 = CredibilityInformation(Seq(Credit(5), Debit(3)))
      val id2 = createCredibilityId()

      credibilityRepository.updateCredibility(id1, info1).futureValue
      credibilityRepository.updateCredibility(id2, info2).futureValue

      val result = credibilityRepository.findCredibility(id1)

      assert(result.futureValue === info1)
    }
  }

  "updateCredibility" should {
    "overwrite an existing value" in {
      val info1 = CredibilityInformation(Seq(Credit(10), Debit(5)))
      val info2 = CredibilityInformation(Seq(Credit(5), Debit(3)))
      val id = createCredibilityId()

      credibilityRepository.updateCredibility(id, info1).futureValue
      credibilityRepository.updateCredibility(id, info2).futureValue

      val result = credibilityRepository.findCredibility(id)

      assert(result.futureValue === info2)
    }
  }
}
