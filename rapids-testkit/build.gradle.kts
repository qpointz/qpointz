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
    api(project(":rapids-api"))

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter(libs.versions.junit.get())
        }


        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())

            dependencies {
                implementation(project())
                implementation(libs.lombok)
                annotationProcessor(libs.lombok)
            }

            testType.set(TestSuiteType.INTEGRATION_TEST)
        }
    }
}