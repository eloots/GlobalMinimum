import sbt._

object Version {
  val akkaVer         = "2.5.25"
  val logbackVer      = "1.2.3"
  val scalaVer        = "2.12.10"
  val scalaParsersVer = "1.0.6"
  val scalaTestVer    = "3.0.8"

}

object Dependencies {
  val dependencies = Seq(
    "com.typesafe.akka"       %% "akka-actor"                 % Version.akkaVer,
    "com.typesafe.akka"       %% "akka-slf4j"                 % Version.akkaVer,
    "ch.qos.logback"           % "logback-classic"            % Version.logbackVer,
    "org.scala-lang.modules"  %% "scala-parser-combinators"   % Version.scalaParsersVer,
    "com.typesafe.akka"       %% "akka-testkit"               % Version.akkaVer % "test",
    "org.scalatest"           %% "scalatest"                  % Version.scalaTestVer % "test"
  )
}
