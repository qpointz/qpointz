/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/8.0.2/userguide/multi_project_builds.html
 * This project uses @Incubating APIs which are subject to change.
 */

rootProject.name = "mill"

include (":mill-common")
include (":mill-test-common")

include (":mill-common-service")
include (":mill-common-security")



include (":clients:mill-jdbc-driver")
include (":clients:mill-jdbc-shell")

include (":mill-starter-backends")
include (":mill-starter-services")
include (":mill-starter-grpc-service")

include (":mill-service")
include (":mill-azure-service-function")

include (":mill-sample-service") //temp to be removed


dependencyResolutionManagement {

    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {

            val lombok = version("lombok", "1.18.34")
            library("lombok", "org.projectlombok", "lombok").versionRef(lombok)

            val springBootV = version("boot", "3.3.5")
            val springBootG = "org.springframework.boot"
            plugin("spring-dependency-management","io.spring.dependency-management").version("1.1.6")
            plugin("spring-boot-plugin", "org.springframework.boot").versionRef(springBootV)

            //library("spring-test", "org.springframework", "spring-test").version("6.1.14")
            library("spring-security-test", "org.springframework.security", "spring-security-test").version("6.3.4")

            val protobufPluginV = version("protobuf-plugin", "0.9.4")
            plugin("google-protobuf-plugin", "com.google.protobuf").versionRef(protobufPluginV)

            library("boot-devtools", springBootG,"spring-boot-devtools").versionRef(springBootV)
            library("boot-configuration-processor",springBootG,"spring-boot-configuration-processor").versionRef(springBootV)
            library("boot-starter-test",springBootG,"spring-boot-starter-test").versionRef(springBootV)
            library("boot-starter-security",springBootG,"spring-boot-starter-security").versionRef(springBootV)
            library("boot-starter-security-oauth2-client",springBootG, "spring-boot-starter-oauth2-client").versionRef(springBootV)
            library("boot-starter-security-oauth2-resource-server",springBootG, "spring-boot-starter-oauth2-resource-server").versionRef(springBootV)
            library("boot-starter-web",springBootG,"spring-boot-starter-web").versionRef(springBootV)
            library("boot-starter-webflux",springBootG,"spring-boot-starter-webflux").versionRef(springBootV)
            library("boot-starter", springBootG,"spring-boot-starter").versionRef(springBootV)
            library("boot-starter-actuator", springBootG,"spring-boot-starter-actuator").versionRef(springBootV)


            val springCloudV = version("springCloud", "4.1.3")
            val springCloudG = "org.springframework.cloud"
            library("spring-cloud-function-grpc", springCloudG, "spring-cloud-function-grpc").versionRef(springCloudV)
            library("spring-cloud-function-context", springCloudG, "spring-cloud-function-context").versionRef(springCloudV)
            library("spring-cloud-function-adapter-azure", springCloudG, "spring-cloud-function-adapter-azure").versionRef(springCloudV)
            library("spring-cloud-function-adapter-azure-web", springCloudG, "spring-cloud-function-adapter-azure-web").versionRef(springCloudV)
            library("spring-cloud-starter-function-web", springCloudG, "spring-cloud-starter-function-web").versionRef(springCloudV)


            val calcite = version("calcite", "1.38.0")
            library("calcite-core", "org.apache.calcite", "calcite-core").versionRef(calcite)
            library("calcite-server", "org.apache.calcite", "calcite-server").versionRef(calcite)
            library("calcite-testkit", "org.apache.calcite", "calcite-testkit").versionRef("calcite")
            library("calcite-file", "org.apache.calcite", "calcite-file").versionRef("calcite")
            library("calcite-csv", "org.apache.calcite", "calcite-csv").versionRef(calcite)

            val junit = version("junit", "5.11.3")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef(junit)
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef(junit)
            library("junit-vintage-engine", "org.junit.vintage", "junit-vintage-engine").versionRef(junit)

            library("slf4j-api", "org.slf4j", "slf4j-api").version("2.0.16")
            library("logback-core", "ch.qos.logback", "logback-core").version("1.5.12")
            library("logback-classic", "ch.qos.logback", "logback-classic").version("1.5.12")
            library("fusesource-jansi","org.fusesource.jansi", "jansi").version("2.4.1")
            bundle("logging", listOf(
                    "slf4j-api",
                    "logback-core",
                    "logback-classic",
                    "fusesource-jansi"
            ))

            val mockito = version("mockito", "5.14.2")
            library("mockito-core", "org.mockito", "mockito-core").versionRef(mockito)
            library("mockito-junit-jupiter", "org.mockito", "mockito-junit-jupiter").versionRef(mockito)

            val protobuf = version("protobuf", "3.25.5")
            library("protobuf-java", "com.google.protobuf", "protobuf-java").versionRef(protobuf)
            library("protobuf-protoc", "com.google.protobuf", "protoc").versionRef(protobuf)
            library("protobuf-java-util", "com.google.protobuf", "protobuf-java-util").versionRef(protobuf)

            val grpc = version("grpc", "1.68.1")
            library("grpc-protobuf","io.grpc","grpc-protobuf").versionRef(grpc)
            library("grpc-stub","io.grpc","grpc-stub").versionRef(grpc)
            library("grpc-api","io.grpc","grpc-api").versionRef(grpc)
            library("grpc-core","io.grpc","grpc-core").versionRef(grpc)
            library("grpc-testing","io.grpc","grpc-testing").versionRef(grpc)
            library("grpc-inprocess","io.grpc","grpc-inprocess").versionRef(grpc)
            library("grpc-census","io.grpc","grpc-census").versionRef(grpc)
            library("grpc-all","io.grpc","grpc-all").versionRef(grpc)
            library("grpc-context","io.grpc","grpc-context").versionRef(grpc)
            library("grpc-netty-shaded", "io.grpc", "grpc-netty-shaded").versionRef(grpc)


            library("javax-annotation-api" ,"javax.annotation" , "javax.annotation-api").version("1.3.2")

            library ("hadoop-bare-naked-local-fs", "com.globalmentor", "hadoop-bare-naked-local-fs").version("0.1.0")

            library ("h2-database", "com.h2database", "h2").version("2.3.232")

            val apacheCommons = version("apacheCommons", "3.14.0")
            library("apache-commons-lang3","org.apache.commons", "commons-lang3").versionRef(apacheCommons)

            val substrait = version("substrait", "0.45.0")
            library("substrait-core", "io.substrait", "core").versionRef(substrait)
            library("substrait-isthmus", "io.substrait", "isthmus").versionRef(substrait)

            val bootGrpc = version("bootGRPC", "3.1.0.RELEASE")
            library("bootGRPC-server", "net.devh", "grpc-server-spring-boot-starter" ).versionRef(bootGrpc)
            library("bootGRPC-client", "net.devh", "grpc-client-spring-boot-starter" ).versionRef(bootGrpc)

            val jakartaServletApi = version("jakartaServletApi", "6.1.0")
            library("jakarta-servlet-api", "jakarta.servlet", "jakarta.servlet-api").versionRef(jakartaServletApi)

            val googleApiGrpc = version("googleApiGrpc", "2.48.0")
            library("googleapigrpc-proto-common-protos", "com.google.api.grpc", "proto-google-common-protos").versionRef(googleApiGrpc)

            val jackson = version("jackson", "2.18.1")
            library("jackson-core", "com.fasterxml.jackson.core", "jackson-core").versionRef(jackson)
            library("jackson-dataformat-yaml", "com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml").versionRef(jackson)
            library("jackson-datatype-jsr310", "com.fasterxml.jackson.datatype","jackson-datatype-jsr310").versionRef(jackson)

            val guava = version("guava", "33.3.1-jre")
            library("guava", "com.google.guava", "guava").versionRef(guava)

            val okhttp3 = version("okhttp", "4.12.0")
            library("okhttp-mock-webserver", "com.squareup.okhttp3", "mockwebserver").versionRef(okhttp3)
            library("okhttp", "com.squareup.okhttp3", "okhttp").versionRef(okhttp3)

            library("opencensus-impl", "io.opencensus","opencensus-impl").version("0.31.1")

            val sqlline = version("sqlline","1.12.0")
            library("sqlline", "sqlline", "sqlline").versionRef(sqlline)

            library("drivers-postgressql","org.postgresql","postgresql").version("42.7.4")
            library("drivers-sqllite", "org.xerial","sqlite-jdbc").version("3.47.0.0")
            library("drivers-mariadb", "org.mariadb.jdbc","mariadb-java-client").version("3.5.0")
            library("drivers-oracle","com.oracle","ojdbc14").version("10.2.0.4.0")
            library("drivers-trino", "io.trino","trino-jdbc").version("464")
            library("drivers-duckdb", "org.duckdb","duckdb_jdbc").version("1.1.3")
            library("drivers-snowflake","net.snowflake", "snowflake-jdbc").version("3.20.0")
            library("drivers-clickhouse","com.clickhouse", "clickhouse-jdbc").version("0.7.1")


            bundle("jdbc-pack", listOf(
                "h2-database",
                "drivers-postgressql",
                "drivers-sqllite",
                "drivers-mariadb",
                "drivers-trino",
                "drivers-duckdb",
                "drivers-snowflake",
                "drivers-clickhouse",
            ))


        }
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        kotlin("jvm") version "1.9.23"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
