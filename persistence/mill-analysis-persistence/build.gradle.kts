plugins {
    kotlin("jvm")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "JPA persistence adapters for Analysis saved-query catalog"
    publishArtifacts = false
}

dependencies {
    api(project(":services:mill-analysis-api"))
    api(project(":persistence:mill-persistence"))
    implementation(libs.boot.starter.data.jpa)
    implementation(libs.bundles.jackson)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.bundles.logging)
    implementation(kotlin("reflect"))
    runtimeOnly(libs.h2.database)
}

testing {
    suites {
        register<org.gradle.api.plugins.jvm.JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.flyway)
                implementation(libs.assertj.core)
                runtimeOnly(libs.h2.database)
            }
        }

        configureEach {
            if (this is org.gradle.api.plugins.jvm.JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.assertj.core)
                }
            }
        }
    }
}

tasks.named<org.gradle.api.tasks.testing.Test>("testIT") {
    testLogging {
        events("passed", "failed", "skipped")
    }
}
