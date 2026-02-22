plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill data lane auto-configuration"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-service-api"))
    api(project(":core:mill-security-autoconfigure"))
    api(project(":data:mill-data-backend-core"))
    api(project(":data:mill-data-backends"))
    api(project(":metadata:mill-metadata-provider"))
    api(libs.jakarta.servlet.api)
    api(libs.javax.annotation.api)
    implementation(libs.calcite.core)
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.security)
    implementation(libs.bundles.jackson)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)
    runtimeOnly(libs.bundles.logging)
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
                    implementation(libs.boot.starter.actuator)
                    implementation(libs.protobuf.java.util)
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
