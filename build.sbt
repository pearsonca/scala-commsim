scalaVersion := "2.11.0"

sbtVersion := "0.13.5"

name := "scala-commsim"

version := "0.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.3.2"
