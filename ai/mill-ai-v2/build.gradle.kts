import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias (libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}


mill {
    description = "Natural language to sql (NL2SQL) core library"
    publishArtifacts = false
}

dependencies {
    implementation(project(":core:mill-core"))
    implementation(libs.pebble.templates)
    implementation(libs.boot.starter)
    implementation(libs.jackson.core)
    implementation(libs.apache.commons.codec)
    implementation(libs.caffeine)
    api(libs.spring.ai.client.chat)
    implementation(libs.spring.ai.starter.model.openai)
    implementation(libs.boot.starter.webflux)
    api(libs.spring.ai.vector.store)
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
                    implementation(project(":ai:mill-ai-v2-test"))
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