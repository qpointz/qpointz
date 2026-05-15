plugins {
    `java-library`
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill ADLS / Azure Blob Storage Spring Boot auto-configuration"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-core"))
    api(project(":cloud:azure:mill-cloud-azure-blob"))
    implementation(libs.azure.storage.blob)
    implementation(libs.azure.identity)
    implementation(libs.boot.starter)
    annotationProcessor(libs.boot.configuration.processor)
    compileOnly(libs.bundles.logging)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit.jupiter)
                implementation(libs.slf4j.api)
                implementation(libs.logback.core)
                implementation(libs.logback.classic)
                implementation(libs.testcontainers.core)
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.azure.storage.blob)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.assertj.core)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
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
