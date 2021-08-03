import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / crossScalaVersions := Seq("2.12.14", "2.13.6")

val epimetheusV = "0.4.0"
val catsV = "2.1.1"
val catsEffectV = "2.1.4"

val log4catsV = "1.3.1"

val specs2V = "4.12.3"

lazy val `epimetheus-log4cats` = project.in(file("."))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .aggregate(core)

lazy val core = project.in(file("core"))
  .settings(
    name := "epimetheus-log4cats",
    libraryDependencies ++= Seq(
      "io.chrisdavenport"           %% "epimetheus"                 % epimetheusV,
      "org.typelevel"               %% "cats-core"                  % catsV,

      "org.typelevel"               %% "cats-effect"                % catsEffectV,

      "org.typelevel"           %% "log4cats-core"              % log4catsV,
      "org.typelevel"           %% "log4cats-testing"           % log4catsV     % Test,

      "org.specs2"                  %% "specs2-core"                % specs2V       % Test,
      "org.specs2"                  %% "specs2-scalacheck"          % specs2V       % Test
    )
  )

lazy val site = project.in(file("site"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(DavenverseMicrositePlugin)
  .dependsOn(core)
  .settings(
    micrositeDescription := "Epimetheus Log4cats Metrics",
  )


