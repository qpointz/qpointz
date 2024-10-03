plugins {
    application
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.4"
    mill
}

mill {
    description = "calcite service desc"
    publishToSonatype = false
}

springBoot {
    mainClass = "io.qpointz.mill.services.CalciteMillService"
}

application {
    mainClass = springBoot.mainClass
    applicationName = "mill-calcite-backend-service"
}

copyDistro("installDist", "main" )
copyDistro("installBootDist", "boot")

fun copyDistro(tk:String, distName: String) {
    tasks.findByName(tk)!!.doLast {
        val distName = distributions.getByName(distName).distributionBaseName.get()
        val outdir = project.layout.buildDirectory.dir("install/${distName}").get()
        copy {
            from(layout.projectDirectory.dir("config/default"))
            into(outdir.dir("config"))
        }
        copy {
            from(rootProject.layout.projectDirectory.dir("../etc/data/datasets/airlines/csv"))
            into(outdir.dir("examples/data/airlines"))
        }
    }
}

dependencies {
    implementation(project(":mill-common-backend-service"))
    implementation(project(":mill-calcite-service"))
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(libs.calcite.file)

    runtimeOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.substrait.isthmus)


    developmentOnly(libs.boot.devtools)
    annotationProcessor(libs.boot.configuration.processor)
    testImplementation(libs.boot.starter.test)    
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
