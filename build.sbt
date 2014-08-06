scalaVersion := "2.11.2"

sbtVersion := "0.13.5"

scalacOptions ++= Seq("-feature","-deprecation")

name := "scala-commsim"

version := "0.1"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4",
  "org.scalatest" %% "scalatest" % "2.1.7",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2"
)
