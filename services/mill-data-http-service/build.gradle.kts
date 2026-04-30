plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Access service implementation for HTTP 1.1 protocol"
    publishArtifacts = true
}

dependencies {
    implementation(project(":core:mill-spring-support"))
    implementation(project(":data:mill-data-backends"))
    implementation(project(":data:mill-data-autoconfigure"))
    implementation(project(":services:mill-service-api"))
    implementation(project(":metadata:mill-metadata-core"))
    implementation(project(":metadata:mill-metadata-autoconfigure"))
    implementation(libs.protobuf.java.util)

    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.webmvc)
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
