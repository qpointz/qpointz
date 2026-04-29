plugins {
    `java-library`
    kotlin("jvm")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "gRPC data plane (grpc-java Netty) — Spring Boot integration without net.devh"
    publishArtifacts = true
}

dependencies {
    implementation(project(":core:mill-spring-support"))
    implementation(project(":data:mill-data-backends"))
    implementation(project(":data:mill-data-autoconfigure"))
    implementation(project(":data:formats:mill-data-format-text"))
    implementation(kotlin("reflect"))
    api(libs.grpc.netty.shaded)
    api(libs.grpc.stub)
    api(libs.grpc.protobuf)
    api(libs.grpc.core)
    implementation(libs.jakarta.annotation.api)
    api(libs.boot.starter.security)
    api(libs.boot.starter.security.oauth2.client)
    api(libs.boot.starter.security.oauth2.resource.server)
    implementation(libs.boot.starter)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.datatype.jsr310)
    api(libs.googleapigrpc.proto.common.protos)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(project(":data:mill-data-autoconfigure"))
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
                implementation(libs.grpc.testing)
                compileOnly(libs.lombok)
                annotationProcessor(libs.lombok)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-backends"))
                    implementation(libs.boot.starter.test)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.mockito.kotlin)
                    implementation(libs.grpc.testing)
                    implementation(libs.h2.database)
                    compileOnly(libs.lombok)
                    annotationProcessor(libs.lombok)
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
