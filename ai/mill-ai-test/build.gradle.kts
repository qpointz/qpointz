import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill AI v3 conversation scenario harness and regression packs"
    publishArtifacts = false
}

dependencies {
    api(project(":ai:mill-ai"))
    implementation(project(":metadata:mill-metadata-core"))
    implementation(project(":data:mill-data-schema-core"))
    implementation(project(":core:mill-sql"))
    api(libs.junit.jupiter.api)
    implementation(libs.mockito.core)
    implementation(libs.mockito.junit.jupiter)
    implementation(libs.assertj.core)
    implementation(libs.bundles.jackson)
    implementation(libs.json.path)
    implementation(libs.slf4j.api)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(project(":ai:mill-ai-autoconfigure"))
                implementation(libs.boot.starter)
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
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
        exceptionFormat = TestExceptionFormat.SHORT
        showStandardStreams = true
    }
}

tasks.named<Test>("test") {
    testLogging {
        showStandardStreams = true
    }
}

repositories {
    mavenCentral()
}
