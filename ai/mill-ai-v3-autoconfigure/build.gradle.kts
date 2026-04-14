plugins {
    kotlin("jvm")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill AI v3 — Spring Boot autoconfiguration for ai/v3 beans"
    publishArtifacts = false
}

dependencies {
    api(project(":ai:mill-ai-v3"))
    api(project(":ai:mill-ai-v3-service"))
    implementation(project(":core:mill-sql"))
    implementation(project(":ai:mill-ai-v3-data"))
    compileOnly(project(":data:mill-data-schema-core"))
    compileOnly(project(":data:mill-data-backend-core"))
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.webflux)
    implementation(libs.langchain4j.open.ai)
    annotationProcessor(libs.boot.configuration.processor)
    compileOnly(project(":ai:mill-ai-v3-persistence"))
    compileOnly(project(":persistence:mill-persistence"))
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                // PersistenceAutoConfiguration handles DataSource/Flyway/JPA bootstrap
                // so the IT context gets a fully wired Spring Boot JPA stack.
                runtimeOnly(project(":persistence:mill-persistence-autoconfigure"))
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.assertj.core)
                    implementation(project(":ai:mill-ai-v3-persistence"))
                    implementation(project(":persistence:mill-persistence"))
                    implementation(libs.boot.starter.data.jpa)
                    runtimeOnly(libs.h2.database)
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

repositories {
    mavenCentral()
}
