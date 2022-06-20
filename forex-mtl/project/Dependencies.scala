import sbt._

object Dependencies {

  object Versions {
    val cats       = "2.7.0"
    val catsEffect = "3.3.12"
    val fs2        = "3.2.8"
    val http4s     = "0.23.12"
    val circe      = "0.13.0"
    val pureConfig = "0.17.1"
    val tsec       = "0.4.0"

    val kindProjector  = "0.10.3"
    val logback        = "1.2.3"
    val scalaLogging   = "3.9.4"
    val scalaCheck     = "1.15.3"
    val scalaTest      = "3.2.7"
    val catsScalaCheck = "0.3.0"
    val docker         = "0.40.8"
    val scalatestplus  = "3.1.0.0-RC2"
  }

  object Libraries {
    def circe(artifact: String): ModuleID  = "io.circe"   %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s

    lazy val cats       = "org.typelevel" %% "cats-core"   % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val fs2        = "co.fs2"        %% "fs2-core"    % Versions.fs2

    lazy val http4sDsl       = http4s("http4s-dsl")
    lazy val http4sServer    = http4s("http4s-blaze-server")
    lazy val http4sClient    = http4s("http4s-blaze-client")
    lazy val http4sCirce     = http4s("http4s-circe")
    lazy val tsec            = "io.github.jmcardon" %% "tsec-http4s" % Versions.tsec
    lazy val circeCore       = circe("circe-core")
    lazy val circeGeneric    = circe("circe-generic")
    lazy val circeGenericExt = circe("circe-generic-extras")
    lazy val circeParser     = circe("circe-parser")
    lazy val pureConfig      = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig

    // Compiler plugins
    lazy val kindProjector = "org.typelevel" %% "kind-projector" % Versions.kindProjector

    // Runtime
    lazy val logback = "ch.qos.logback"             % "logback-classic" % Versions.logback
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging"  % Versions.scalaLogging

    // Test
    lazy val scalaTest      = "org.scalatest"     %% "scalatest"                      % Versions.scalaTest
    lazy val scalaCheck     = "org.scalacheck"    %% "scalacheck"                     % Versions.scalaCheck
    lazy val catsScalaCheck = "io.chrisdavenport" %% "cats-scalacheck"                % Versions.catsScalaCheck
    lazy val scalaTestPlus  = "org.scalatestplus" %% "scalatestplus-scalacheck"       % Versions.scalatestplus
    lazy val docker         = "com.dimafeng"      %% "testcontainers-scala-scalatest" % Versions.docker

    val All: Seq[ModuleID] = Seq(
      compilerPlugin(Libraries.kindProjector),
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.fs2,
      Libraries.tsec,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sClient,
      Libraries.http4sCirce,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeGenericExt,
      Libraries.circeParser,
      Libraries.pureConfig,
      Libraries.logback,
      Libraries.scalaLogging,
      Libraries.scalaTestPlus  % Test,
      Libraries.scalaTest      % Test,
      Libraries.scalaCheck     % Test,
      Libraries.catsScalaCheck % Test,
      Libraries.docker         % Test
    )
  }

}
