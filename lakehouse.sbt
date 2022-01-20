import Dependencies._
import sbt.Keys.libraryDependencies
import sbt._
import BuildUtils._

lazy val `lakehouse` = project
  .aggregate(
    `lakehouse-cli`
  )

lazy val `lakehouse-cli` = libProjectNoDependencies("lakehouse","lakehouse-cli")
  .settings(
    libraryDependencies ++= modules(
      apacheSpark.sql,
      apacheSpark.core,
      apacheHadoop.client,
      apacheHadoop.common,
      apacheHadoop.aws,
      minio.minio,
      "io.netty" % "netty-transport-native-epoll" % "4.1.73.Final" % Provided//,
      //"io.delta" %% "delta-core" % "1.0.0"
    )
    ,
    libraryDependencies += "com.google.guava" % "guava" % "31.0.1-jre" % Provided,
    libraryDependencies += amazonAWSSDK.sdkJava % Provided,
    libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.1"

  )