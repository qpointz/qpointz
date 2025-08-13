plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
    `java-library`
    jacoco
}

shadow {
    //archiveBaseName("lala")
    //baseN
}

dependencies {
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(project(":rapids-grpc"))
    implementation(libs.avatica.core)
    implementation(libs.avatica.server)

    implementation(libs.lombok)
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
                    implementation(project(":rapids-test-kit"))
                    implementation(project(":rapids-grpc-service"))
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