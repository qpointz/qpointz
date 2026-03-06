plugins {
    application
    alias(libs.plugins.spring.boot.plugin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "calcite service desc"
    publishArtifacts = false

    editions {
        defaultEdition = "basic"

        // Feature flags can be pure build toggles with no dependency changes.
        // feature("metadata") {
        //     description = "Core metadata capabilities"
        // }


        // feature("aiv1") {
        //     description = "AI v1 NL-SQL chat support"
        //     module(":ai:mill-ai-v1-nlsql-chat-service")
        // }

        edition("basic") {
            description = "Base metadata-only edition"
           // feature("metadata")
        }
        // edition("edition2") {
        //     description = "Metadata + AI v1 edition"
        //     features("metadata", "aiv1")
        // }
    }
}

springBoot {
    mainClass = "io.qpointz.mill.app.MillService"
    application {
        applicationName = "mill-service"
    }
}

tasks.register<Sync>("assembleSamples") {
    group = "distribution"
    description = "Installs sample data "

    val datasetsDir = rootProject.layout.projectDirectory.dir("test/datasets")
    val samplesDir = layout.projectDirectory.dir("src/main/docker/samples")

    //into(layout.buildDirectory.dir("install/samples"))
    into(project.layout.buildDirectory.dir("install/samples"))

    from(rootProject.layout.projectDirectory.dir(".certs")) { into("certs") }

    // moneta sample
    from(datasetsDir.file("moneta/moneta-slim.sql")) { into("data/moneta") }
    from(datasetsDir.file("moneta/moneta.sql")) { into("data/moneta") }
    //from(datasetsDir.files("moneta/moneta-meta.yaml", "moneta/moneta-meta-repository.yaml")) { into("etc") }
    // skymill sample
    from(datasetsDir.dir("skymill/parquet")) { into("data/skymill") }
    //from(datasetsDir.file("skymill/skymill.sql")) {
    //    into("data/skymill")
    //    rename { "skymill-slim.sql" }
    //}
    //from(datasetsDir.files("skymill/skymill-meta.yaml", "skymill/skymill-meta-repository.yaml")) { into("etc") }

    //from(samplesDir.files("application-moneta.yml", "application-moneta-slim.yml", "application-skymill.yml", "application-skymill-slim.yml")) {
    //    into("config")
    //}
}



dependencies {
    implementation(project(":services:mill-well-known-service"))

    implementation(project(":data:mill-data-autoconfigure"))
    implementation(project(":data:mill-data-backends"))
    implementation(project(":services:mill-data-grpc-service"))
    implementation(project(":services:mill-data-http-service"))
    implementation(project(":data:mill-data-source-core"))
    implementation(project(":data:mill-data-source-calcite"))
    implementation(project(":data:formats:mill-data-format-text"))
    implementation(project(":data:formats:mill-data-format-excel"))
    implementation(project(":data:formats:mill-data-format-avro"))
    implementation(project(":data:formats:mill-data-format-parquet"))
    implementation(project(":data:formats:mill-data-format-arrow"))


    //implementation(project(":ai:mill-ai-v1-nlsql-chat-service"))
    //implementation(project(":ui:mill-grinder-service"))
    //implementation(project(":ui:mill-grinder-service"))

    //implementation(project(":metadata:mill-metadata-autoconfigure"))
    //implementation(project(":metadata:mill-metadata-service"))


    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.springdoc.openapi.starter.webflux.api)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.boot.starter.security)
    implementation(libs.boot.starter.security.oauth2.client)
    implementation(libs.boot.starter.security.oauth2.resource.server)
    //implementation(libs.spring.ai.starter.model.openai)
    //implementation(libs.spring.ai.starter.model.azureopenai)
    //implementation(libs.spring.ai.starter.model.ollama)
    implementation(libs.boot.starter.actuator)
    implementation(libs.boot.starter)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.jdbc.pack)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                }
            }
        }
    }
}