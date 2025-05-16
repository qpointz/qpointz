plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    mill
    `mill-publish`
}

mill {
    description = "LLM simple service implementation"
    publishToSonatype = false
}

dependencies {
    implementation(libs.boot.starter)
    implementation(libs.jackson.core)

    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    runtimeOnly(libs.apache. httpclient5)
    runtimeOnly(libs.apache.httpcore5)
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
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.web)
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
