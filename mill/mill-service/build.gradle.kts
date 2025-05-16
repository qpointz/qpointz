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
    }
}

dependencies {
    implementation(project(":mill-common-security"))
    implementation(project(":services:mill-grpc-service"))
    implementation(project(":services:mill-starter-services"))
    implementation(project(":mill-starter-backends"))
    //implementation(project(":ai:mill-ai-llm-service"))
    //implementation(project(":ai:mill-ai-mcp-service"))
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