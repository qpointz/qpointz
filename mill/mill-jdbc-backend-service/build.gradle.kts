plugins {
    application
    id("org.springframework.boot") version libs.versions.boot
    id("io.spring.dependency-management") version "1.1.4"
    mill
}

mill {
    publishToSonatype = false
}

springBoot {
    mainClass = "io.qpointz.mill.services.JdbcMillService"
}

application {
    mainClass = "io.qpointz.mill.services.JdbcMillService"
    applicationName = "mill-jdbc-backend-service"
}


dependencies {
    implementation(project(":mill-common-backend-service"))
    implementation(project(":mill-calcite-service"))
    implementation(project(":mill-jdbc-service"))
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
                    implementation(libs.calcite.core)
                    implementation(libs.calcite.csv)
                    implementation(libs.calcite.file)
                    implementation(libs.h2.database)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
