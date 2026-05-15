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
    api(project(":core:mill-core"))
    api(project(":cloud:gcp:mill-cloud-gcp-blob"))
    compileOnly(libs.google.cloud.storage)
    implementation(libs.boot.starter)
    compileOnly(libs.bundles.logging)
    annotationProcessor(libs.boot.configuration.processor)
    testImplementation(libs.google.cloud.storage)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
                implementation(libs.testcontainers.core)
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.google.cloud.storage)
            }
        }

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

tasks.named<Test>("testIT") {
    testLogging { events("passed", "failed", "skipped") }
}
