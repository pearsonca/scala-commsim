scalaVersion := "2.11.0"

sbtVersion := "0.13.5"

scalacOptions ++= Seq("-feature","-deprecation")

name := "scala-commsim"

version := "0.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.2",
  "org.scalatest" %% "scalatest" % "2.1.7"
)
