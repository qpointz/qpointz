plugins {
    application
    alias(libs.plugins.spring.boot.plugin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill service application. Assembly point for all editions."
    publishArtifacts = false

    editions {
        defaultEdition = "minimal"

        feature("sample-data") {
            description = "Provides Sample datasets"
        }

        feature("sample-certs") {
            description = "Provide sample self-signed certificates"
        }
        
        edition("minimal") {
            description = "Base metadata-only edition"
        }

        edition("integration") {
            description = "Integration edition used for internal integration testing"
            from("minimal")
            feature("sample-data")
            feature("sample-certs")
        }

        edition("samples") {
            description = "Mill service with sample data"
            from("minimal")
            feature("sample-data")

        }

    }
}

springBoot {
    mainClass = "io.qpointz.mill.app.MillService"
    application {
        applicationName = "mill-service"
    }
}

val installSampleData = tasks.register<Copy>("installSampleData") {
    group = "distribution"
    description = "Installs sample data "

    onlyIf("sample-data enabled") {
        mill.editions.isActive("sample-data").get()
    }

    val datasetsDir = rootProject.layout.projectDirectory.dir("test/datasets")
    val editionInstallDir = tasks.named<Sync>("installBootDist").map { it.destinationDir }

    into(editionInstallDir)

    // moneta sample
    from(datasetsDir.file("moneta/moneta-slim.sql")) { into("etc/data/moneta") }
    from(datasetsDir.file("moneta/moneta.sql")) { into("etc/data/moneta") }

    //skymill
    from(datasetsDir.dir("skymill/parquet")) { into("etc/data/skymill") }
}

val installSampleCerts = tasks.register<Copy>("installSampleCerts") {
    group = "distribution"
    description = "Installs sample certs"

    onlyIf("sample-certs enabled") {
        mill.editions.isActive("sample-certs").get()
    }

    val editionInstallDir = tasks.named<Sync>("installBootDist").map { it.destinationDir }

    into(editionInstallDir)
    from(rootProject.layout.projectDirectory.dir(".certs")) { into("etc/certs") }


}

tasks.named("installBootDist") {
    finalizedBy(installSampleData, installSampleCerts)
}


dependencies {
    implementation(project(":services:mill-service-common"))
    implementation(project(":security:mill-security-autoconfigure"))
    implementation(project(":security:mill-security-auth-service"))
    implementation(project(":security:mill-security-persistence"))

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

    implementation(project(":metadata:mill-metadata-autoconfigure"))
    implementation(project(":metadata:mill-metadata-service"))
    runtimeOnly(project(":metadata:mill-metadata-persistence"))

    //implementation(project(":ai:mill-ai-v1-nlsql-chat-service"))
    //implementation(project(":ui:mill-grinder-service"))


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