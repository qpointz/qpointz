plugins {
    application
    alias(libs.plugins.spring.boot.plugin)
    mill
}

mill {
    description = "calcite service desc"
    publishToSonatype = false
}

springBoot {
    mainClass = "io.qpointz.mill.services.MillService"
    application {
        applicationName = "mill-service"
    }
}

copyDistro("installDist", "main" )
copyDistro("installBootDist", "boot")

fun copyDistro(tk:String, distributionName: String) {
    tasks.findByName(tk)!!.doLast {
        val distBaseName = distributions.getByName(distributionName).distributionBaseName.get()
        val buildDir = project.layout.buildDirectory
        val outDir = buildDir.dir("install/${distBaseName}").get()
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
            into(outDir.file("etc/sample/moneta"))
            rename { fileName -> "moneta.sql" }
        }
    }
}

dependencies {
    implementation(project(":mill-common-security"))
    implementation(project(":services:mill-jet-grpc-service"))
    implementation(project(":services:mill-jet-http-service"))
    implementation(project(":services:mill-starter-services"))
    implementation(project(":mill-starter-backends"))
    //implementation(project(":services:mill-ai-llm-service"))
    implementation(project(":services:mill-ai-mcp-service"))
    implementation(project(":services:mill-grinder-service"))
    implementation(project(":ai:mill-ai-nlsql-chat-service"))


    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-api:2.8.9")
    implementation("io.micrometer:micrometer-registry-prometheus:1.15.1")

    runtimeOnly(libs.spring.ai.starter.model.openai)
    runtimeOnly(libs.spring.ai.starter.model.azureopenai)

    implementation(libs.boot.starter.actuator)
    implementation(libs.boot.starter)
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