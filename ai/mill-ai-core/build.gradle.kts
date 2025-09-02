import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Natural language to sql (NL2SQL) core library"
    publishArtifacts = false
}

dependencies {
    api("io.qpointz.mill:mill-service-core")
    implementation(libs.pebble.templates)
    implementation(libs.boot.starter)
    implementation(libs.jackson.core)
    implementation(libs.apache.commons.codec)
    api(libs.spring.ai.client.chat)
    api(libs.spring.ai.vector.store)
    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    runtimeOnly(libs.apache.httpclient5)
    runtimeOnly(libs.apache.httpcore5)
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
                    implementation("io.qpointz.mill:mill-security-core")
                    implementation("io.qpointz.mill:mill-starter-service")
                    implementation("io.qpointz.mill:mill-starter-backends")
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.web)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.h2.database)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
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
