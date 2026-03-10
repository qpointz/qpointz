import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill AI v3 test support skeleton"
    publishArtifacts = false
}

dependencies {
    api(project(":ai:mill-ai-v3-core"))
    api(libs.junit.jupiter.api)
    implementation(libs.mockito.core)
    implementation(libs.mockito.junit.jupiter)
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.assertj.core)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(project(":ai:mill-ai-v3-core"))
                implementation(project(":ai:mill-ai-v3-capabilities"))
                implementation(project(":ai:mill-ai-v3-langchain4j"))
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
    }
}

repositories {
    mavenCentral()
}
