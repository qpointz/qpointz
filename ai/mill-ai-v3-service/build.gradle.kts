plugins {
    kotlin("jvm")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill AI v3 — HTTP/SSE service facade (REST controllers, OpenAPI docs)"
    publishArtifacts = false
}

dependencies {
    api(project(":ai:mill-ai-v3"))
    implementation(project(":ai:mill-ai-v3-data"))
    implementation(project(":core:mill-core"))
    implementation(libs.boot.starter.webflux)
    implementation(libs.springdoc.openapi.starter.webflux.api)
    implementation(libs.bundles.jackson)
    compileOnly(libs.bundles.logging)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project(":ai:mill-ai-v3-autoconfigure"))
                implementation(libs.h2.database)
                implementation(libs.boot.starter.data.jpa)
                implementation(project(":persistence:mill-persistence"))
                implementation(project(":persistence:mill-persistence-autoconfigure"))
                runtimeOnly(project(":ai:mill-ai-v3-persistence"))
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.webflux)
                    implementation(libs.assertj.core)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.mockito.kotlin)
                }
            }
        }
    }
}

repositories {
    mavenCentral()
}
