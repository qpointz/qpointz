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
        from(rootProject.layout.projectDirectory.dir("../test/datasets/airlines/csv"))
        into(outDir.dir("etc/sample/airlines"))
    }

    copy {
        from(rootProject.layout.projectDirectory.dir("../test/datasets/users/sql"))
        into(outDir.dir("etc/sample/users"))
    }

    copy {
        from(rootProject.layout.projectDirectory.file("../test/datasets/moneta/moneta-slim.sql"))
        into(outDir.file("etc/sample/moneta-slim"))
        rename { fileName -> "moneta.sql" }
    }

    copy {
        from(rootProject.layout.projectDirectory.dir("../test/datasets/moneta/"))
        into(outDir.dir("etc/sample/moneta"))
    }
}

tasks.register("assembleSamples") {
    group = "distribution"
    description = "Installs sample data "
    doLast {
        val outDir = project.layout.buildDirectory.dir("install/samples").get()
        val datasetsDir = rootProject.layout.projectDirectory.dir("../test/datasets/")

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
    implementation("io.qpointz.mill:mill-starter-service")

    runtimeOnly("io.qpointz.mill:mill-starter-backends")
    runtimeOnly("io.qpointz.mill:mill-grinder-service")
    runtimeOnly("io.qpointz.mill:mill-jet-grpc-service")
    runtimeOnly("io.qpointz.mill:mill-jet-http-service")

    runtimeOnly("io.qpointz.mill:mill-ai-nlsql-chat-service")

    runtimeOnly(libs.springdoc.openapi.starter.webmvc.ui)
    runtimeOnly(libs.springdoc.openapi.starter.webflux.api)
    runtimeOnly(libs.micrometer.registry.prometheus)

    runtimeOnly(libs.boot.starter.security)
    runtimeOnly(libs.boot.starter.security.oauth2.client)
    runtimeOnly(libs.boot.starter.security.oauth2.resource.server)

    runtimeOnly(libs.spring.ai.starter.model.openai)
    runtimeOnly(libs.spring.ai.starter.model.azureopenai)
    runtimeOnly(libs.spring.ai.starter.model.ollama)

    runtimeOnly(libs.boot.starter.actuator)
    runtimeOnly(libs.boot.starter)
    runtimeOnly(libs.bundles.logging)
    runtimeOnly(libs.bundles.jdbc.pack)
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