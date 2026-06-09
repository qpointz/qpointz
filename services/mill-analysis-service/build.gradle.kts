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
    description = "HTTP Analysis API (/api/v1/analysis/**)"
    publishArtifacts = true
}

dependencies {
    implementation(project(":core:mill-core"))
    implementation(project(":core:mill-sql"))
    implementation(project(":core:mill-spring-support"))
    implementation(project(":services:mill-service-api"))
    implementation(project(":services:mill-analysis-api"))
    implementation(project(":data:mill-data-backend-core"))
    implementation(libs.boot.starter.webmvc)
    implementation(libs.boot.starter.security)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.logging)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())
            dependencies {
                implementation(project())
                implementation(project(":core:mill-sql"))
                implementation(project(":persistence:mill-analysis-persistence"))
                implementation(project(":data:mill-data-backend-core"))
                implementation(platform(libs.boot.dependencies))
                implementation("org.springframework.security:spring-security-test")
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.webmvc)
                implementation(libs.boot.starter.webmvc.test)
                implementation(libs.boot.starter.security)
                implementation(libs.boot.starter.data.jpa)
                implementation(libs.boot.starter.flyway)
                implementation(libs.assertj.core)
                implementation(libs.mockito.kotlin)
                runtimeOnly(libs.h2.database)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.webmvc.test)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.mockito.kotlin)
                }
            }
        }
    }
}

tasks.named<Test>("test") {
    testLogging {
        events("passed", "failed", "skipped")
    }
}

tasks.named<Test>("testIT") {
    testLogging {
        events("passed", "failed", "skipped")
    }
}
