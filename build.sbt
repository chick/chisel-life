

val defaultVersions = Map(
  "chisel3" -> "3.5-SNAPSHOT",
  "chisel-iotesters" -> "2.5-SNAPSHOT"
)

lazy val commonSettings = Seq(
  organization := "edu.berkeley.cs",
  version := "0.4-SNAPSHOT",
  scalaVersion := "2.12.13",
  crossScalaVersions := Seq("2.12.13", "2.13.6"),
  scalacOptions := Seq("-deprecation", "-feature", "-language:reflectiveCalls"),
  libraryDependencies ++= Seq("chisel3","chisel-iotesters").map {
    dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep))
  },
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.9.2",
    "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    "org.apache.logging.log4j" % "log4j-api" % "2.11.2",
    "org.apache.logging.log4j" % "log4j-core" % "2.11.2"
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.mavenLocal
  )
)

lazy val life = (project in file("."))
  .settings(commonSettings)
  .settings(scalacOptions in Test ++= Seq("-language:reflectiveCalls"))
  .settings(fork := true)
