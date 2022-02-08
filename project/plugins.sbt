addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.7")
addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.2.2")
addDependencyTreePlugin
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")
//addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.1")
//addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.6.5")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.1.0")
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.4.11-30-75fb3441")