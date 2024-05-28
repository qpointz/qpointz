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
include (":delta-jdbc-driver")
include (":delta-lineage")

include ( ":rapids-navigator-api")

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
            library("boot-starter", "org.springframework.boot","spring-boot-starter").versionRef(boot)



            val calcite = version("calcite", "1.37.0")
            library("calcite-core", "org.apache.calcite", "calcite-core").versionRef(calcite)
            library("calcite-server", "org.apache.calcite", "calcite-server").versionRef(calcite)
            library("calcite-testkit", "org.apache.calcite", "calcite-testkit").versionRef("calcite")
            library("calcite-file", "org.apache.calcite", "calcite-file").versionRef("calcite")
            library("calcite-csv", "org.apache.calcite", "calcite-csv").versionRef(calcite)
//
//            val avatica = version("avatica", "1.23.0")
//            library("avatica-core", "org.apache.calcite.avatica", "avatica-core").versionRef(avatica)
//            library("avatica-server", "org.apache.calcite.avatica", "avatica-server").versionRef(avatica)

//            version("rest-assured", "5.3.0")
//            library("rest-assured", "io.rest-assured", "rest-assured").versionRef("rest-assured")
//
//            val quarkus = version("quarkus", "2.16.6.Final")
//            library("quarkus-bom", "io.quarkus.platform", "quarkus-bom").versionRef(quarkus)
//            library("quarkus-arc", "io.quarkus", "quarkus-arc").versionRef(quarkus)
//            library("quarkus-resteasy-reactive", "io.quarkus", "quarkus-resteasy-reactive").versionRef(quarkus)
//            library("quarkus-resteasy-reactive-jackson", "io.quarkus", "quarkus-resteasy-reactive-jackson").versionRef(quarkus)
//            library("quarkus-junit5", "io.quarkus", "quarkus-junit5").versionRef(quarkus)
//            library("quarkus-config-yaml", "io.quarkus", "quarkus-config-yaml").versionRef(quarkus)
//            library("quarkus-vertx", "io.quarkus", "quarkus-vertx").versionRef(quarkus)
//            library("quarkus-container-image-docker", "io.quarkus", "quarkus-container-image-docker").versionRef(quarkus)
//
//            val apacheArrowFlight = version("arrow", "11.0.0")
//            library("arrow-format", "org.apache.arrow", "arrow-format").versionRef(apacheArrowFlight)
//            library("arrow-jdbc", "org.apache.arrow", "arrow-jdbc").versionRef(apacheArrowFlight)
//            library("arrow-vector", "org.apache.arrow", "arrow-vector").versionRef(apacheArrowFlight)
//            library("arrow-flight-core", "org.apache.arrow", "flight-core").versionRef(apacheArrowFlight)
//            library("arrow-flight-grpc", "org.apache.arrow", "flight-grpc").versionRef(apacheArrowFlight)
//            library("arrow-flight-sql", "org.apache.arrow", "flight-sql").versionRef(apacheArrowFlight)
//
//            val apacheMinaFtpServer = version("minaftpserver", "1.2.0")
//            library("apache-mina-ftpserver-core", "org.apache.ftpserver", "ftpserver-core").versionRef(apacheMinaFtpServer)
//            library("apache-mina-ftpserver-ftplet-api", "org.apache.ftpserver", "ftplet-api").versionRef(apacheMinaFtpServer)
//            library("apache-mina-ftpserver", "org.apache.ftpserver", "ftpserver").versionRef(apacheMinaFtpServer)
//
/*            val apacheParuet = version("apacheParquet", "1.13.1")
            library("parquet-avro","org.apache.parquet", "parquet-avro").versionRef(apacheParuet)
            library("parquet-common","org.apache.parquet", "parquet-common").versionRef(apacheParuet)
            library("parquet-column","org.apache.parquet", "parquet-column").versionRef(apacheParuet)
            library("parquet-hadoop","org.apache.parquet", "parquet-hadoop").versionRef(apacheParuet)

            val apacheHadoop = version("apacheHadoop", "3.3.6")
            library("hadoop-common","org.apache.hadoop", "hadoop-common" ).versionRef(apacheHadoop)
            library("hadoop-client","org.apache.hadoop", "hadoop-client" ).versionRef(apacheHadoop)

            val apacheAvro = version("apacheAvro", "1.11.3")
            library("avro", "org.apache.avro", "avro").versionRef(apacheAvro)
            library("avro-mapred", "org.apache.avro", "avro-mapred").versionRef(apacheAvro)
*/
            val junit = version("junit", "5.10.2")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef(junit)
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef(junit)
            library("junit-vintage-engine", "org.junit.vintage", "junit-vintage-engine").versionRef(junit)
/*
            library("azure-storage-file-datalake", "com.azure", "azure-storage-file-datalake").version("12.18.0")
            library("azure-storage-blob-nio", "com.azure", "azure-storage-blob-nio").version("12.0.0-beta.19")
*/
//            val vertx = version("vertx", "4.5.5")
//            library("vertx-core", "io.vertx", "vertx-core").versionRef(vertx)
//            library("vertx-grpc", "io.vertx", "vertx-grpc").versionRef(vertx)
//            library("vertx-grpc-server", "io.vertx", "vertx-grpc-server").versionRef(vertx)
//            library("vertx-grpc-client", "io.vertx", "vertx-grpc-client").versionRef(vertx)
//            library("vertx-grpc-protoc-plugin2", "io.vertx", "vertx-grpc-protoc-plugin2").versionRef(vertx)

//            val smallrye = version("smallrye", "3.4.4")
//            library("smallrye-config", "io.smallrye.config", "smallrye-config").versionRef(smallrye)
//            library("smallrye-config-source-yaml", "io.smallrye.config", "smallrye-config-source-yaml").versionRef(smallrye)
//
//            val microprofile = version("microprofile", "3.1")
//            library("microprofile-config-api", "org.eclipse.microprofile.config", "microprofile-config-api").versionRef(microprofile)

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


//            val postgre = version("postgre", "42.7.0")
//            library("postgresql", "org.postgresql","postgresql").versionRef(postgre)
//
//            val sdl = version("sdl","2.11.2")
//            library("sdl-odata-service","com.sdl", "odata_service").versionRef(sdl)
//            library("sdl-odata-common","com.sdl", "odata_common").versionRef(sdl)
//            library("sdl-odata-api","com.sdl", "odata_api").versionRef(sdl) //- Framework APIs
//            library("sdl-odata-processor","com.sdl", "odata_parser").versionRef(sdl) //- OData URI parser
//            library("sdl-odata-renderer","com.sdl", "odata_renderer").versionRef(sdl) //- Renderers for Atom and JSON output
//            library("sdl-odata-edm","com.sdl", "odata_edm").versionRef(sdl) //- The OData EDM metadata (Entity Data Model)
//
/*            val olingo = version("olingo", "4.10.0")
            library("olingo-odata-server-core", "org.apache.olingo", "odata-server-core").versionRef(olingo)
            library("olingo-odata-server-api", "org.apache.olingo", "odata-server-api").versionRef(olingo)
            library("olingo-odata-commons-api", "org.apache.olingo", "odata-commons-api").versionRef(olingo)
            library("olingo-odata-commons-core", "org.apache.olingo", "odata-commons-core").versionRef(olingo)
*/
/*            val jetty = version("jetty","9.4.48.v20220622")
            library("jetty-server", "org.eclipse.jetty","jetty-server").versionRef(jetty)
            library("jetty-servlet", "org.eclipse.jetty","jetty-servlet").versionRef(jetty)
            library("jetty-security", "org.eclipse.jetty","jetty-security").versionRef(jetty)
            library("jetty-openid", "org.eclipse.jetty","jetty-openid").versionRef(jetty)
*/
            val mockito = version("mockito", "5.12.0")
            library("mockito-core", "org.mockito", "mockito-core").versionRef(mockito)
            library("mockito-junit-jupiter", "org.mockito", "mockito-junit-jupiter").versionRef(mockito)

            val protobuf = version("protobuf", "3.24.0")
            library("protobuf-java", "com.google.protobuf", "protobuf-java").versionRef(protobuf)
            library("protobuf-protoc", "com.google.protobuf", "protoc").versionRef(protobuf)

            val grpc = version("grpc", "1.64.0")
            library("grpc-protobuf","io.grpc","grpc-protobuf").versionRef(grpc)
            library("grpc-stub","io.grpc","grpc-stub").versionRef(grpc)

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

//            val scala = version("scala", "2.13.12")
//            library("scala-library", "org.scala-lang", "scala-library").versionRef(scala)
//            library("scala-reflect", "org.scala-lang", "scala-reflect").versionRef(scala)
//            library("scala-compiler", "org.scala-lang", "scala-compiler").versionRef(scala)

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
