plugins {
    kotlin("jvm")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill security — JPA persistence adapters for user identity, credentials, groups, and profiles"
    publishArtifacts = false
}

dependencies {
    api(project(":security:mill-security"))
    api(project(":persistence:mill-persistence"))
    implementation(project(":core:mill-spring-support"))
    implementation(project(":security:mill-service-security"))
    implementation(libs.boot.starter.data.jpa)
    implementation(libs.boot.starter.security)
    implementation(kotlin("reflect"))
    runtimeOnly(libs.h2.database)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
                implementation(libs.boot.starter.security)
                implementation(project(":security:mill-service-security"))
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
    testLogging {
        events("passed", "failed", "skipped")
    }
}

repositories {
    mavenCentral()
}
