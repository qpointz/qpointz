plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Natural language to sql (NL2SQL) chat service"
    publishArtifacts = false
}

dependencies {
    api(project(":ai:mill-ai-v1-core"))
    implementation(libs.boot.starter.data.jpa)
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.security)
    implementation(libs.boot.starter.webflux)
    implementation(libs.spring.ai.starter.model.chat.memory.repository.jdbc)
    implementation(libs.jackson.core)
    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

testing {
    suites {

        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(libs.h2.database)
                implementation(libs.boot.starter.data.jpa)
                implementation(libs.spring.ai.starter.model.openai)
            }

        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":core:mill-service-security"))
                    implementation(project(":data:mill-data-backends"))
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.webflux)
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
