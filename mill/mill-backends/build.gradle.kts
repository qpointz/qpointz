plugins {
    //id("org.springframework.boot") version libs.versions.boot
    id("io.spring.dependency-management") version "1.1.4"
    mill
    `mill-publish`
    `java-library`
}

mill {
    description = "Library provides base Mill Service implementation"
    publishToSonatype = true
}


dependencies {
    api(project(":mill-common-service"))
    api(project(":mill-common"))
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(libs.calcite.file)
    implementation(libs.boot.starter)
    implementation(libs.substrait.isthmus)
    compileOnly(libs.lombok)
    runtimeOnly(libs.bundles.logging)
    runtimeOnly(libs.h2.database)
    annotationProcessor(libs.lombok)
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
                    implementation(project(":mill-grpc-service"))
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
