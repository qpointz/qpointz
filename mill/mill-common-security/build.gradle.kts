plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    mill
    `mill-publish`
}

mill {
    publishToSonatype = true
}

dependencies {
    api(libs.boot.starter.security)

    implementation(libs.boot.starter.security.oauth2.resource.server)
    implementation(libs.jackson.core)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.javax.annotation.api)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)
    implementation(libs.boot.starter.web)

    compileOnly(libs.lombok)
    runtimeOnly(libs.bundles.logging)

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
                    implementation(project(":mill-test-common"))
                    implementation(libs.boot.starter.test)

                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.h2.database)
                    implementation(libs.lombok)
                    implementation(libs.boot.starter.test)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
