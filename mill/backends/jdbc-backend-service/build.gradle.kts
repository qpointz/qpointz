plugins {
    application
    id("org.springframework.boot") version libs.versions.boot
    id("io.spring.dependency-management") version "1.1.4"
}

springBoot {
    mainClass = "io.qpointz.mill.service.JdbcMillService"
}

application {
    mainClass = "io.qpointz.mill.service.JdbcMillService"
    applicationName = "jdbc-backend-service"
}

//tasks.getByName("installDist").doLast {
//    val distName = distributions.getByName("main").distributionBaseName.get()
//    val outdir = project.layout.buildDirectory.dir("install/${distName}").get()
//
//    copy {
//        from(layout.projectDirectory.dir("etc/config/default"))
//        into(outdir.dir("config"))
//    }
//    copy {
//        from(rootProject.layout.projectDirectory.dir("../etc/data/datasets/airlines/csv"))
//        into(outdir.dir("examples/data/airlines"))
//    }
//}

dependencies {
    implementation(project(":backends:backend-core"))
    implementation(project(":backends:calcite-backend-service"))
    implementation(libs.calcite.core)
    runtimeOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    developmentOnly(libs.boot.devtools)
    annotationProcessor(libs.boot.configuration.processor)
    testImplementation(libs.boot.starter.test)
    testImplementation("io.projectreactor:reactor-test")
    runtimeOnly(libs.h2.database)
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
