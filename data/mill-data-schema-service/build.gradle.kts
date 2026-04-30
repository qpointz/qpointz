import org.gradle.api.tasks.testing.Test
import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill physical schema explorer REST service"
    publishArtifacts = false
}

dependencies {
    implementation(project(":data:mill-data-schema-core"))
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.webmvc)
    implementation(libs.bundles.jackson)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.bundles.logging)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())
            dependencies {
                implementation(project())
                implementation(project(":data:mill-data-schema-core"))
                implementation(project(":data:mill-data-backend-core"))
                implementation(project(":data:mill-data-autoconfigure"))
                implementation(project(":metadata:mill-metadata-autoconfigure"))
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.webmvc)
                implementation(libs.h2.database)
            }
        }
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-backend-core"))
                    implementation(project(":metadata:mill-metadata-autoconfigure"))
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.webmvc.test)
                    implementation(libs.boot.starter.webmvc)
                    implementation(libs.mockito.kotlin)
                }
            }
        }
    }
}

tasks.named<Test>("testIT") {
    testLogging { events("passed", "failed", "skipped") }
}
