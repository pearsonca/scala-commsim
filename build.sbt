scalaVersion := "2.11.5"

sbtVersion := "0.13.7"

scalacOptions ++= Seq("-feature","-deprecation","-target:jvm-1.8")

name := "scala-commsim"

version := "0.2"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4",
  "org.scalatest" %% "scalatest" % "2.2.4",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"
)
