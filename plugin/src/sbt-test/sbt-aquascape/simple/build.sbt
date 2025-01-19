
lazy val myaquascapes = project
  .in(file("myaquascapes"))
  .settings(
    Compile / run / fork := true,
    scalaVersion := "3.6.2",
    libraryDependencies += "com.github.zainab-ali" %% "aquascape" % aquascapeVersion
  )

lazy val docs = project
  .in(file("myproject-docs"))
  .enablePlugins(MdocPlugin, AquascapePlugin)
  // important: do not use `dependsOn(myaquascapes)`
  .settings(
    aquascapeProject := myaquascapes,
  )
