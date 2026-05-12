import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "HTTP query-result execution sessions (/api/v1/query)"
    publishArtifacts = true
}

dependencies {
    implementation(project(":core:mill-core"))
    implementation(project(":core:mill-spring-support"))
    implementation(project(":services:mill-service-api"))
    implementation(project(":data:mill-data-query"))
    implementation(project(":data:mill-data-autoconfigure"))
    implementation(project(":data:mill-data-backend-core"))
    implementation(project(":data:formats:mill-data-format-text"))
    implementation(libs.boot.starter.webmvc)
    implementation(libs.boot.starter.security)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.logging)
    annotationProcessor(libs.boot.configuration.processor)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())

            targets {
                all {
                    testTask.configure {
                        systemProperty(
                            "skymill.datasets.dir",
                            rootProject.file("test/datasets/skymill").absolutePath,
                        )
                    }
                }
            }

            dependencies {
                implementation(project())
                implementation(project(":data:mill-data-autoconfigure"))
                implementation(platform(libs.boot.dependencies))
                implementation("org.springframework.security:spring-security-test")
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.webmvc)
                implementation(libs.boot.starter.webmvc.test)
                implementation(libs.assertj.core)
                implementation(libs.h2.database)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                }
            }
        }
    }
}

tasks.named<Test>("testIT") {
    testLogging {
        events("passed", "failed", "skipped")
    }
}
