import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm")
    alias (libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill AI test kit"
    publishArtifacts = false
}

dependencies {
    implementation(project(":core:mill-core"))
    implementation(project(":ai:mill-ai-v2"))
    implementation(libs.spring.ai.client.chat)
    implementation(libs.json.path)
    api(libs.junit.jupiter.api)
    implementation(libs.mockito.core)
    implementation(libs.mockito.junit.jupiter)
    compileOnly(libs.bundles.logging)
    runtimeOnly(libs.apache.httpclient5)
    runtimeOnly(libs.apache.httpcore5)
    implementation(kotlin("stdlib-jdk8"))
}

tasks.named<Javadoc>("javadoc") { //temporary
    isFailOnError = false
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(libs.spring.ai.starter.model.openai)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":core:mill-core"))
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.boot.starter.test)
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