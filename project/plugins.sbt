addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.8.0")
addSbtPlugin("org.typelevel" % "sbt-typelevel-mergify" % "0.8.0")
addSbtPlugin("org.typelevel" % "sbt-typelevel-site" % "0.8.0")
addSbtPlugin("org.typelevel" % "sbt-typelevel-scalafix" % "0.8.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.1")
addSbtPlugin("com.siriusxm" % "sbt-snapshot4s" % "0.2.3")
addDependencyTreePlugin
resolvers += "central-snapshots" at "https://central.sonatype.com/repository/maven-snapshots"
addSbtPlugin("pink.cozydev" % "protosearch-sbt" % "0.0-2915ac1-SNAPSHOT")
