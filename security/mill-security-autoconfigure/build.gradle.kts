plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill security — Spring autoconfiguration: SecurityFilterChain wiring, AuthenticationManager, authentication method providers"
    publishArtifacts = false
}

dependencies {
    api(project(":security:mill-security"))
    api(project(":security:mill-service-security"))
    implementation(project(":core:mill-core"))
    implementation(project(":core:mill-spring-support"))
    implementation(libs.boot.starter.security)
    implementation(libs.boot.starter.security.oauth2.resource.server)
    implementation(libs.boot.starter.security.oauth2.client)
    implementation(libs.boot.starter.web)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)

    runtimeOnly(libs.bundles.logging)
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
                }
            }
        }
    }
}

tasks.named<Test>("testIT") {
    testLogging { events("passed", "failed", "skipped") }
}
