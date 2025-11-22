import com.typesafe.tools.mima.core._
import snapshot4s.BuildInfo.snapshot4sVersion
import aquascapebuild.AquascapeDirectives
Global / onChangedBuildSource := ReloadOnSourceChanges
// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.4" // your current series x.y

ThisBuild / organization := "io.github.zainab-ali"
ThisBuild / organizationName := "Zainab Ali"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("zainab-ali", "Zainab Ali")
)

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

val Scala3 = "3.3.6"
ThisBuild / crossScalaVersions := Seq(Scala3)
ThisBuild / scalaVersion := Scala3 // the default Scala

lazy val root = tlCrossRootProject.aggregate(core)

ThisBuild / tlCiMimaBinaryIssueCheck := false

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("core"))
  .settings(
    name := "aquascape",
    fork := true,
    mimaPreviousArtifacts := Set.empty,
    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-core" % "3.12.2",
      "co.fs2" %%% "fs2-io" % "3.12.2",
      "org.creativescala" %%% "doodle-core" % "0.32.0",
      "org.typelevel" %%% "cats-core" % "2.13.0",
      "org.typelevel" %%% "cats-effect" % "3.6.3",
      "org.scalameta" %%% "munit" % "1.2.1" % Test,
      "org.typelevel" %%% "cats-effect-testkit" % "3.6.3" % Test,
      "org.typelevel" %%% "munit-cats-effect" % "2.1.0" % Test,
      "com.siriusxm" %%% "snapshot4s-munit" % snapshot4sVersion % Test
    ),
    buildInfoKeys := Seq[BuildInfoKey](ThisBuild / baseDirectory),
    buildInfoPackage := "aquascape"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.creativescala" %% "doodle-java2d" % "0.32.0",
      "com.monovore" %% "decline" % "2.5.0"
    )
  )
  .jsSettings(
    Test / fork := false,
    Test / scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
    },
    libraryDependencies += ("org.creativescala" %%% "doodle-svg" % "0.32.0")
      .exclude(
        "com.lihaoyi",
        "sourcecode_sjs1_3"
      ), // Both doodle-svg and snapshot4s include sourcecode.
    mimaPreviousArtifacts := Set.empty
  )
  .enablePlugins(BuildInfoPlugin, Snapshot4sPlugin)

lazy val siteUtils = project
  .in(file("site-utils"))
  .settings(
    name := "aquascape-site",
    Test / scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
    },
    libraryDependencies ++= Seq(
      ("org.creativescala" %%% "doodle-svg" % "0.32.0")
        .exclude(
          "com.lihaoyi",
          "sourcecode_sjs1_3"
        ), // Both doodle-svg and scalameta include sourcecode.
      ("org.scalameta" %%% "scalameta" % "4.9.9" % Compile)
        .cross(CrossVersion.for3Use2_13),
      ("org.scalameta" %% "scalafmt-core" % "3.8.3" % Compile)
        .cross(CrossVersion.for3Use2_13),
      "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      "org.typelevel" %%% "cats-effect-testkit" % "3.6.3",
      "org.scalameta" %%% "munit" % "1.2.1" % Test,
      "org.typelevel" %%% "munit-cats-effect" % "2.1.0" % Test,
      ("com.siriusxm" %%% "snapshot4s-munit" % snapshot4sVersion % Test)
        .exclude(
          "com.lihaoyi",
          "sourcecode_sjs1_3"
        ) // Both snapshot4s and scalameta include sourcecode.
    )
  )
  .dependsOn(core.js)
  .enablePlugins(ScalaJSPlugin, NoPublishPlugin, Snapshot4sPlugin)

lazy val referenceGuide = project
  .in(file("reference-guide"))
  .dependsOn(siteUtils)
  .enablePlugins(ScalaJSPlugin, NoPublishPlugin)

lazy val symbolGuide = project
  .in(file("symbol-guide"))
  .dependsOn(siteUtils, referenceGuide)
  .enablePlugins(ScalaJSPlugin, NoPublishPlugin)

import laika.format._
import laika.ast.Path.Root
import laika.helium.config.{ThemeNavigationSection, TextLink}

lazy val docs = project
  .in(file("site"))
  .dependsOn(core.jvm)
  .settings(
    mdocVariables += ("SCALAJS_VERSION" -> scalaJSVersion),
    tlSiteKeepFiles := false,
    tlSiteHelium := tlSiteHelium.value.site
      .mainNavigation(
        prependLinks = Seq(
          ThemeNavigationSection(
            "About",
            TextLink.internal(Root / "README.md", "About aquascape")
          )
        )
      )
      .site
      .internalCSS(Root / "main.css")
      .site
      .internalJS(Root / "main.js")
      .site
      // Add highlight.js for code examples
      .internalJS(Root / "highlight.min.js")
      .site
      .internalJS(Root / "aquascape.js")
      .site
      .internalJS(Root / "symbol-guide.js")
      .site
      .internalCSS(Root / "a11y-dark.min.css"),
    laikaExtensions += AquascapeDirectives,
    // Add symbol guide and reference doc JS
    tlSite := Def
      .sequential(
        Compile / clean,
        symbolGuide / Compile / fastOptJS,
        mdoc.toTask(""),
        Def.task {
          val symbolGuideJS =
            (symbolGuide / Compile / fastOptJS / artifactPath).value
          IO.copyFile(symbolGuideJS, mdocOut.value / "symbol-guide.js")
        },
        laikaSite
      )
      .value
  )
  .enablePlugins(TypelevelSitePlugin, NoPublishPlugin)
