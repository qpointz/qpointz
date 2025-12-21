plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}

mill {
    publishArtifacts = true
}

dependencies {
    api(libs.boot.starter.security)

    implementation(project(":core:mill-core"))
    implementation(libs.boot.starter.security.oauth2.resource.server)
    implementation(libs.boot.starter.security.oauth2.client)
    implementation(libs.boot.starter.web)
    implementation(libs.okhttp)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)

    compileOnly(libs.lombok)

    runtimeOnly(libs.bundles.logging)

    testImplementation(libs.boot.starter.test)
}


testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":core:mill-test-kit"))
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
