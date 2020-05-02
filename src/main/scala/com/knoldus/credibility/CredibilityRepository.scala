package com.knoldus.credibility

import java.io._
import java.nio.file.{Files, Path, Paths}

import akka.Done

import scala.concurrent.{ExecutionContext, Future}

trait CredibilityRepository {
  def updateCredibility(
                         credibilityId: CredibilityId,
                         credibilityInformation: CredibilityInformation): Future[Done]

  def findCredibility(credibilityId: CredibilityId): Future[CredibilityInformation]
}

class InMemoryCredibilityRepository(implicit ec: ExecutionContext)
  extends CredibilityRepository {
  var data: Map[CredibilityId, CredibilityInformation] = Map.empty

  override def updateCredibility(
                                  credibilityId: CredibilityId,
                                  credibilityInformation: CredibilityInformation): Future[Done] = Future {
    data = data.updated(credibilityId, credibilityInformation)
    Done
  }

  override def findCredibility(credibilityId: CredibilityId)
  : Future[CredibilityInformation] = Future {
    data(credibilityId)
  }
}

class FileBasedCredibilityRepository(rootPath: Path)(implicit ec: ExecutionContext)
  extends CredibilityRepository {

  rootPath.toFile.mkdirs()

  override def updateCredibility(
                                  credibilityId: CredibilityId,
                                  credibilityInformation: CredibilityInformation
  ): Future[Done] = {
    Future {
      val file = new File(rootPath.toFile, credibilityId.value)

      Files.write(
        Paths.get(file.getAbsolutePath),
        credibilityInformation.adjustments.map(_.absoluteValue).mkString(",").getBytes
      )

      Done
    }
  }


  override def findCredibility(credibilityId: CredibilityId): Future[CredibilityInformation]
  = {
    Future {
      val file = new File(rootPath.toFile, credibilityId.value)

      val fileContents = new String(
        Files.readAllBytes(Paths.get(file.getAbsolutePath))
      )

      val adjustments = fileContents.split(",").toSeq.map(_.toInt).map {
        case money if money >= 0 => Credit(money)
        case money if money < 0 => Debit(-money)
      }

      CredibilityInformation(adjustments)
    }
  }
}
