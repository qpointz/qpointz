/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/8.0.2/userguide/multi_project_builds.html
 * This project uses @Incubating APIs which are subject to change.
 */

rootProject.name = "delta"

include (":delta-core")
include (":delta-service-core")
include (":delta-service-calcite")
include (":test-kit:core-test-kit")


dependencyResolutionManagement {

    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {

            val lombok = version("lombok", "1.18.32")
            library("lombok", "org.projectlombok", "lombok").versionRef(lombok)

            val boot = version("boot", "3.3.0")
            library("boot-devtools", "org.springframework.boot","spring-boot-devtools").versionRef(boot)
            library("boot-configuration-processor","org.springframework.boot","spring-boot-configuration-processor").versionRef(boot)
            library("boot-starter-test","org.springframework.boot","spring-boot-starter-test").versionRef(boot)
            library("boot-starter-security","org.springframework.boot","spring-boot-starter-security").versionRef(boot)
            library("boot-starter-web","org.springframework.boot","spring-boot-starter-web").versionRef(boot)
            library("boot-starter-webflux","org.springframework.boot","spring-boot-starter-webflux").versionRef(boot)
            library("boot-starter", "org.springframework.boot","spring-boot-starter").versionRef(boot)


            val calcite = version("calcite", "1.36.0")
            library("calcite-core", "org.apache.calcite", "calcite-core").versionRef(calcite)
            library("calcite-server", "org.apache.calcite", "calcite-server").versionRef(calcite)
            library("calcite-testkit", "org.apache.calcite", "calcite-testkit").versionRef("calcite")
            library("calcite-file", "org.apache.calcite", "calcite-file").versionRef("calcite")
            library("calcite-csv", "org.apache.calcite", "calcite-csv").versionRef(calcite)

            val junit = version("junit", "5.10.2")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef(junit)
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef(junit)
            library("junit-vintage-engine", "org.junit.vintage", "junit-vintage-engine").versionRef(junit)

            library("slf4j-api", "org.slf4j", "slf4j-api").version("2.0.13")
            library("logback-core", "ch.qos.logback", "logback-core").version("1.5.6")
            library("logback-classic", "ch.qos.logback", "logback-classic").version("1.5.6")
            library("fusesource-jansi","org.fusesource.jansi", "jansi").version("2.4.1")
            bundle("logging", listOf(
                    "slf4j-api",
                    "logback-core",
                    "logback-classic",
                    "fusesource-jansi"
            ))

            val spring = version("spring", "6.1.1")
            library("spring-context", "org.springframework","spring-context").versionRef(spring)
            library("spring-web","org.springframework","spring-web").versionRef(spring)
            library("spring-webmvc","org.springframework","spring-webmvc").versionRef(spring)

            val mockito = version("mockito", "5.12.0")
            library("mockito-core", "org.mockito", "mockito-core").versionRef(mockito)
            library("mockito-junit-jupiter", "org.mockito", "mockito-junit-jupiter").versionRef(mockito)

            val protobuf = version("protobuf", "3.24.0")
            library("protobuf-java", "com.google.protobuf", "protobuf-java").versionRef(protobuf)
            library("protobuf-protoc", "com.google.protobuf", "protoc").versionRef(protobuf)

            val grpc = version("grpc", "1.64.0")
            library("grpc-protobuf","io.grpc","grpc-protobuf").versionRef(grpc)
            library("grpc-stub","io.grpc","grpc-stub").versionRef(grpc)
            library("grpc-api","io.grpc","grpc-api").versionRef(grpc)
            library("grpc-core","io.grpc","grpc-core").versionRef(grpc)
            library("grpc-testing","io.grpc","grpc-testing").versionRef(grpc)

            library("javax-annotation-api" ,"javax.annotation" , "javax.annotation-api").version("1.3.2")

            library ("hadoop-bare-naked-local-fs", "com.globalmentor", "hadoop-bare-naked-local-fs").version("0.1.0")

            library ("h2-database", "com.h2database", "h2").version("2.2.224")

            val apacheCommons = version("apacheCommons", "3.14.0")
            library("apache-commons-lang3","org.apache.commons", "commons-lang3").versionRef(apacheCommons)

            val substrait = version("substrait", "0.31.0")
            library("substrait-core", "io.substrait", "core").versionRef(substrait)
            library("substrait-isthmus", "io.substrait", "isthmus").versionRef(substrait)

            val bootGrpc = version("bootGRPC", "3.1.0.RELEASE")
            library("bootGRPC-server", "net.devh", "grpc-server-spring-boot-starter" ).versionRef(bootGrpc)
            library("bootGRPC-client", "net.devh", "grpc-client-spring-boot-starter" ).versionRef(bootGrpc)

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
