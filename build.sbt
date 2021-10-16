name := "credibility"

organization := "knoldus"

version := "0.2-SNAPSHOT"

scalaVersion := "2.13.6"

lazy val akkaHttpVersion = "10.2.6"
lazy val akkaVersion    = "2.6.17"
lazy val akkaManagementVersion =  "1.1.1"
lazy val logbackVersion = "1.2.6"
lazy val scalaTestVersion = "3.2.10"

fork := true
ThisBuild / parallelExecution := false

scalacOptions ++= Seq(
  "-deprecation",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-cluster"         % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding"% akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
  "com.swissborg" %% "lithium" % "0.11.2",

  //Logback
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,

  //Test dependencies
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion% Test
)
