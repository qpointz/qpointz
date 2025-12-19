addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")
addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.2.7")
addDependencyTreePlugin
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.1")
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.5.4-14-7cb43276")
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always