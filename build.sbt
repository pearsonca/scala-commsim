import com.typesafe.sbt.SbtStartScript

seq(SbtStartScript.startScriptForClassesSettings: _*)

mainClass in (Compile, run) := Some("edu.cap10.actormodels.covert.SynthUser")

scalaVersion := "2.11.6"

sbtVersion := "0.13.7"

scalacOptions ++= Seq("-feature","-deprecation","-target:jvm-1.8")

name := "scala-commsim"

version := "0.2"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.3" % "test",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"
)

libraryDependencies  ++= Seq(
  "org.scalanlp" %% "breeze" % "0.11.2"//,
  // native libraries are not included by default. add this if you want them (as of 0.7)
  // native libraries greatly improve performance, but increase jar sizes. 
  // It also packages various blas implementations, which have licenses that may or may not
  // be compatible with the Apache License. No GPL code, as best I know.
  // "org.scalanlp" %% "breeze-natives" % "0.11.2"
)
