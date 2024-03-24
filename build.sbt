Global / onChangedBuildSource := ReloadOnSourceChanges
// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.1" // your current series x.y

ThisBuild / organization := "com.github.zainab-ali"
ThisBuild / organizationName := "Zainab Ali"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("zainab-ali", "Zainab Ali")
)

// publish to s01.oss.sonatype.org (set to true to publish to oss.sonatype.org instead)
ThisBuild / tlSonatypeUseLegacyHost := true

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

val Scala3 = "3.4.0"
ThisBuild / crossScalaVersions := Seq(Scala3)
ThisBuild / scalaVersion := Scala3 // the default Scala

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "aquascape",
    fork := true,
    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-core" % "3.9.4",
      "co.fs2" %%% "fs2-io" % "3.9.4",
      ("org.creativescala" %%% "doodle" % "0.21.0")
        .exclude(org = "com.lihaoyi", name = "sourcecode_3"),
      "org.typelevel" %%% "cats-core" % "2.10.0",
      "org.typelevel" %%% "cats-effect" % "3.5.4",
      "org.typelevel" %% "cats-effect-testkit" % "3.5.4" % Test,
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      ("org.scalameta" %%% "scalameta" % "4.9.2" % Test)
        .cross(CrossVersion.for3Use2_13),
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test,
      ("org.scalameta" %% "scalafmt-core" % "3.8.0" % Test)
        .cross(CrossVersion.for3Use2_13),
      ("com.lihaoyi" %% "sourcecode" % "0.3.1" % Test)
        .cross(CrossVersion.for3Use2_13),
      ("com.lihaoyi" %% "pprint" % "0.8.1" % Test)
        .cross(CrossVersion.for3Use2_13)
    ),
    buildInfoKeys := Seq[BuildInfoKey](ThisBuild / baseDirectory),
    buildInfoPackage := "aquascape"
  )
  .enablePlugins(BuildInfoPlugin)

import laika.format._
import laika.ast.Path.Root

lazy val docs = project
  .in(file("site"))
  .dependsOn(core.jvm)
  .settings(
    tlSiteKeepFiles := false,
    tlSiteHelium := tlSiteHelium.value.site
      .internalCSS(Root / "main.css"),
    tlSite := Def
      .sequential(
        Compile / clean,
        (core.jvm / Test / test),
        mdoc.toTask(""),
        laikaSite
      )
      .value
  )
  .enablePlugins(TypelevelSitePlugin)
