plugins {
    `java-library`
    jacoco
    id("jacoco-report-aggregation")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":rapids-api"))

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
                    implementation(project(":rapids-testkit"))

                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)

                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}