plugins {
    application
    alias(libs.plugins.spring.boot.plugin)
    id("io.qpointz.plugins.mill")
}

mill {
    description = "calcite service desc"
    publishArtifacts = false
}

springBoot {
    mainClass = "io.qpointz.mill.services.MillService"
    application {
        applicationName = "mill-service"
    }
}

tasks.register("assembleSamples") {
    group = "distribution"
    description = "Installs sample data "
    doLast {
        val outDir = project.layout.buildDirectory.dir("install/samples").get()
        val datasetsDir = rootProject.layout.projectDirectory.dir("test/datasets/")
        logger.warn(datasetsDir.toString())

        //moneta sample
        copy {
            from(datasetsDir.file("moneta/moneta-slim.sql"))
            into(outDir.file("data/moneta"))
        }

        copy {
            from(datasetsDir.file("moneta/moneta.sql"))
            into(outDir.file("data/moneta"))
        }

        copy {
            from(datasetsDir.file("moneta/moneta-meta.yaml"))
            from(datasetsDir.file("moneta/moneta-meta-repository.yaml"))
            into(outDir.file("etc"))
        }

        //skymill sample
        copy {
            from(datasetsDir.file("skymill/skymill.sql"))
            into(outDir.file("data/skymill"))
        }

        copy {
            from(datasetsDir.file("skymill/skymill.sql"))
            into(outDir.file("data/skymill"))
            rename { it -> "skymill-slim.sql" }
        }

        copy {
            from(datasetsDir.file("skymill/skymill-meta.yaml"))
            from(datasetsDir.file("skymill/skymill-meta-repository.yaml"))
            into(outDir.file("etc"))
        }

        val monetaSample = project.layout.projectDirectory.dir("src/main/docker/samples")
        copy {
            from(monetaSample.file("application-moneta.yml"))
            from(monetaSample.file("application-moneta-slim.yml"))
            from(monetaSample.file("application-skymill.yml"))
            from(monetaSample.file("application-skymill-slim.yml"))
            into(outDir.file("config"))
        }

    }
}



dependencies {
    implementation(project(":services:mill-well-known-service"))
    implementation(project(":services:mill-metadata-service"))
    implementation(project(":data:mill-data-backends"))
    implementation(project(":data:mill-data-grpc-service"))
    implementation(project(":data:mill-data-http-service"))

    implementation(project(":ai:mill-ai-v1-nlsql-chat-service"))

    implementation(project(":ui:mill-grinder-service"))

    implementation(project(":ui:mill-grinder-service"))

    implementation(project(":source:mill-source-core"))
    implementation(project(":source:mill-source-calcite"))
    implementation(project(":source:formats:mill-source-format-text"))
    implementation(project(":source:formats:mill-source-format-excel"))
    implementation(project(":source:formats:mill-source-format-avro"))
    implementation(project(":source:formats:mill-source-format-parquet"))

    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.springdoc.openapi.starter.webflux.api)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.boot.starter.security)
    implementation(libs.boot.starter.security.oauth2.client)
    implementation(libs.boot.starter.security.oauth2.resource.server)
    implementation(libs.spring.ai.starter.model.openai)
    implementation(libs.spring.ai.starter.model.azureopenai)
    implementation(libs.spring.ai.starter.model.ollama)
    implementation(libs.boot.starter.actuator)
    implementation(libs.boot.starter)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.jdbc.pack)
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