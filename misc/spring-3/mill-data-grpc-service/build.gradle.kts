plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

/**
 * Legacy **net.devh** gRPC Spring Boot starters are pinned here only so the root
 * `libs.versions.toml` stays free of `bootGRPC` (product uses raw grpc-java). Version aligns with
 * [grpc-ecosystem/grpc-spring](https://github.com/grpc-ecosystem/grpc-spring) 3.1.0.RELEASE (Spring Boot 3.2.x line).
 */
val netDevhGrpcSpringBootVersion = "3.1.0.RELEASE"

mill {
    description = "Access service implementation for GRPC protocol"
    publishArtifacts = true
}

dependencies {
    implementation(project(":core:mill-spring-support"))
    implementation(project(":data:mill-data-backends"))
    implementation(project(":data:mill-data-autoconfigure"))
    api(libs.grpc.netty.shaded)
    implementation(libs.jakarta.annotation.api)
    api(libs.boot.starter.security)
    api(libs.boot.starter.security.oauth2.client)
    api(libs.boot.starter.security.oauth2.resource.server)
    api(libs.grpc.core)
    api(libs.jakarta.servlet.api)
    implementation(libs.jackson.dataformat.yaml)
    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    api("net.devh:grpc-server-spring-boot-starter:$netDevhGrpcSpringBootVersion")
    api(libs.googleapigrpc.proto.common.protos)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-backends"))
                    implementation(libs.boot.starter.test)
                    implementation("net.devh:grpc-client-spring-boot-starter:$netDevhGrpcSpringBootVersion")
                    implementation(libs.grpc.testing)
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
