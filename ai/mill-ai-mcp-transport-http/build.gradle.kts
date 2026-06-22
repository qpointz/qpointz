plugins {
    java
    kotlin("jvm")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill AI MCP — Streamable HTTP servlet transport for mill-service"
    publishArtifacts = false
}

dependencies {
    api(project(":ai:mill-ai-mcp-core"))
    implementation(project(":ai:mill-ai"))
    implementation(project(":ai:mill-ai-service"))
    implementation(project(":services:mill-service-api"))
    implementation(platform(libs.mcp.bom))
    implementation(libs.mcp)
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.webmvc)
    implementation(libs.boot.starter.jackson)
    implementation(libs.bundles.jackson)
    annotationProcessor(libs.boot.configuration.processor)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)

    compileOnly(project(":security:mill-security"))
    compileOnly(libs.boot.starter.security)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project(":ai:mill-ai-autoconfigure"))
                implementation(project(":security:mill-security-autoconfigure"))
                implementation(platform(libs.mcp.bom))
                implementation(libs.mcp)
                implementation(libs.h2.database)
                implementation(libs.boot.starter.data.jpa)
                runtimeOnly(project(":persistence:mill-persistence-autoconfigure"))
                compileOnly(project(":security:mill-security"))
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.webmvc.test)
                    implementation(libs.boot.starter.security)
                    implementation(libs.assertj.core)
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
