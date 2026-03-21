plugins {
    kotlin("jvm")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill security — REST auth endpoints (login, logout, me, profile)"
    publishArtifacts = false
}

dependencies {
    api(project(":security:mill-security"))
    implementation(project(":core:mill-spring-support"))
    implementation(project(":security:mill-service-security"))
    implementation(kotlin("reflect"))
    implementation(libs.boot.starter.web)
    implementation(libs.boot.starter.security)
    implementation(libs.boot.starter.security.oauth2.client)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.web)
                implementation(libs.assertj.core)
                implementation(project(":security:mill-service-security"))
                implementation(project(":security:mill-security-persistence"))
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
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.mockito.kotlin)
                }
            }
        }
    }
}

tasks.named<Test>("testIT") {
    testLogging { events("passed", "failed", "skipped") }
}

repositories {
    mavenCentral()
}
