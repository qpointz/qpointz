/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/8.0.2/userguide/multi_project_builds.html
 * This project uses @Incubating APIs which are subject to change.
 */

rootProject.name = "rapids"

include (":rapids-core")
include (":rapids-formats")
include (":rapids-jdbc-driver")
include (":rapids-server")

include (":etc:msynth")
project(":etc:msynth").projectDir = file("etc/msynth")


dependencyResolutionManagement {

    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            version("lombok", "1.18.26")
            library("lombok", "org.projectlombok", "lombok").versionRef("lombok")


            version("calcite", "1.34.0")
            library("calcite-core", "org.apache.calcite", "calcite-core").versionRef("calcite")
            library("calcite-testkit", "org.apache.calcite", "calcite-testkit").versionRef("calcite")
            library("calcite-file", "org.apache.calcite", "calcite-file").versionRef("calcite")
            library("calcite-csv", "org.apache.calcite", "calcite-csv").versionRef("calcite")


            version("avatica", "1.23.0")
            library("avatica-core", "org.apache.calcite.avatica", "avatica-core").versionRef("avatica")
            library("avatica-server", "org.apache.calcite.avatica", "avatica-server").versionRef("avatica")

            version("rest-assured", "5.3.0")
            library("rest-assured", "io.rest-assured", "rest-assured").versionRef("rest-assured")

            val quarkus = version("quarkus", "2.16.6.Final")
            library("quarkus-bom", "io.quarkus", "quarkus-bom").versionRef(quarkus)
            library("quarkus-arc", "io.quarkus", "quarkus-arc").versionRef(quarkus)
            library("quarkus-resteasy-reactive", "io.quarkus", "quarkus-resteasy-reactive").versionRef(quarkus)
            library("quarkus-junit5", "io.quarkus", "quarkus-junit5").versionRef(quarkus)
            library("quarkus-config-yaml", "io.quarkus", "quarkus-config-yaml").versionRef(quarkus)
            library("quarkus-vertx", "io.quarkus", "quarkus-vertx").versionRef(quarkus)

            val apacheArrowFlight = version("arrow", "11.0.0")
            library("arrow-format", "org.apache.arrow", "arrow-format").versionRef(apacheArrowFlight)
            library("arrow-jdbc", "org.apache.arrow", "arrow-jdbc").versionRef(apacheArrowFlight)
            library("arrow-vector", "org.apache.arrow", "arrow-vector").versionRef(apacheArrowFlight)
            library("arrow-flight-core", "org.apache.arrow", "flight-core").versionRef(apacheArrowFlight)
            library("arrow-flight-grpc", "org.apache.arrow", "flight-grpc").versionRef(apacheArrowFlight)
            library("arrow-flight-sql", "org.apache.arrow", "flight-sql").versionRef(apacheArrowFlight)

            val apacheMinaFtpServer = version("minaftpserver", "1.2.0")
            library("apache-mina-ftpserver-core", "org.apache.ftpserver", "ftpserver-core").versionRef(apacheMinaFtpServer)
            library("apache-mina-ftpserver-ftplet-api", "org.apache.ftpserver", "ftplet-api").versionRef(apacheMinaFtpServer)
            library("apache-mina-ftpserver", "org.apache.ftpserver", "ftpserver").versionRef(apacheMinaFtpServer)

            val apacheParuet = version("apacheParquet", "1.13.0")
            library("parquet-avro","org.apache.parquet", "parquet-avro").versionRef(apacheParuet)

            val apacheAvro = version("apacheAvro", "1.11.1")
            library("avro", "org.apache.avro", "avro").versionRef(apacheAvro)
            library("avro-mapred", "org.apache.avro", "avro-mapred").versionRef(apacheAvro)

            val graphQl = version("graphql", "20.2")
            library("graphql-java", "com.graphql-java", "graphql-java").versionRef(graphQl)

            bundle("quarkus-base-impl", listOf("quarkus-arc", "quarkus-config-yaml"))
            bundle("quarkus-base-test", listOf("quarkus-junit5"))
        }
    }
}

pluginManagement {
    val quarkusPluginVersion: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("io.quarkus") version quarkusPluginVersion
    }
}