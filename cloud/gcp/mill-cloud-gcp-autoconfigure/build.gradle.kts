plugins {
    `java-library`
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill GCS Spring Boot auto-configuration"
    publishArtifacts = true
}

dependencies {
    api(project(":cloud:gcp:mill-cloud-gcp-blob"))
    compileOnly(libs.google.cloud.storage)
    implementation(libs.boot.starter)
    compileOnly(libs.bundles.logging)
    annotationProcessor(libs.boot.configuration.processor)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.assertj.core)
                    implementation(libs.slf4j.api)
                    implementation(libs.logback.core)
                    implementation(libs.logback.classic)
                }
            }
        }
    }
}
