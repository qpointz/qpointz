plugins {
    kotlin("jvm")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill metadata — JPA persistence adapters for metadata entities, facet types, and audit log"
    publishArtifacts = false
}

dependencies {
    api(project(":metadata:mill-metadata-core"))
    api(project(":persistence:mill-persistence"))
    implementation(libs.boot.starter.data.jpa)
    implementation(libs.bundles.jackson)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.bundles.logging)
    implementation(kotlin("reflect"))
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
                runtimeOnly(libs.h2.database)
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
                    implementation(libs.mockito.kotlin)
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
