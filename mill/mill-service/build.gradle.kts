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
}

application {
    mainClass = springBoot.mainClass
    applicationName = "mill-service"
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
    implementation(project(":mill-grpc-service"))
    implementation(project(":mill-sample-service"))
    implementation(project(":mill-backends"))
    implementation(libs.boot.starter.web)
    testImplementation(libs.boot.starter.test)
    compileOnly(libs.lombok)
    runtimeOnly(libs.bundles.logging)
    runtimeOnly(libs.bundles.jdbc.pack)
    annotationProcessor(libs.boot.configuration.processor)
    annotationProcessor(libs.lombok)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            testType.set(TestSuiteType.INTEGRATION_TEST)
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.bootGRPC.client)
                    implementation(libs.bootGRPC.server)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.h2.database)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
