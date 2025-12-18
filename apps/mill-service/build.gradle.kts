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

fun copySample(outDirProvider: Provider<Directory>) {
    val outDir = outDirProvider.get()
    copy {
        from(layout.projectDirectory.dir("config/default"))
        into(outDir.dir("config"))
    }

    copy {
        from(project.layout.projectDirectory.dir("config/sample"))
        into(outDir.dir("etc/sample"))
    }

    copy {
        from(rootProject.layout.projectDirectory.dir("test/datasets/airlines/csv"))
        into(outDir.dir("etc/sample/airlines"))
    }

    copy {
        from(rootProject.layout.projectDirectory.dir("test/datasets/users/sql"))
        into(outDir.dir("etc/sample/users"))
    }

    copy {
        from(rootProject.layout.projectDirectory.file("test/datasets/moneta/moneta-slim.sql"))
        into(outDir.file("etc/sample/moneta-slim"))
        rename { fileName -> "moneta.sql" }
    }

    copy {
        from(rootProject.layout.projectDirectory.dir("test/datasets/moneta/"))
        into(outDir.dir("etc/sample/moneta"))
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

        val monetaSample = project.layout.projectDirectory.dir("src/main/docker/samples")
        copy {
            from(monetaSample.file("application-moneta.yml"))
            from(monetaSample.file("application-moneta-slim.yml"))
            into(outDir.file("config"))
        }

    }
}


fun copyDistro(tk:String, distributionName: String) {
    tasks.findByName(tk)!!.doLast {
        logger.warn("Copy copy")
        val distBaseName = distributions.getByName(distributionName).distributionBaseName.get()
        val buildDir = project.layout.buildDirectory
        val outDir = buildDir.dir("install/${distBaseName}")
        copySample(outDir)
    }
}

//copyDistro("installDist", "main" )
//copyDistro("installBootDist", "boot")



dependencies {
    implementation(project(":core:mill-starter-service"))

    implementation(project(":services:mill-metadata-service"))
    implementation(project(":core:mill-starter-backends"))
    implementation(project(":services:mill-grinder-service"))
    implementation(project(":services:mill-jet-grpc-service"))
    implementation(project(":services:mill-jet-http-service"))

    implementation(project(":ai:mill-ai-nlsql-chat-service"))

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