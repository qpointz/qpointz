plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Access service implementation for GRPC protocol"
    publishArtifacts = true
}

dependencies {
    implementation(project(":data:mill-data-backends"))
    implementation(project(":data:mill-data-autoconfigure"))
    api(libs.grpc.netty.shaded)
    implementation(libs.javax.annotation.api)
    api(libs.boot.starter.security)
    api(libs.boot.starter.security.oauth2.client)
    api(libs.boot.starter.security.oauth2.resource.server)
    api(libs.grpc.core)
    api(libs.jakarta.servlet.api)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.datatype.jsr310)
    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    api(libs.bootGRPC.server)
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
                    implementation(libs.bootGRPC.client)
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
