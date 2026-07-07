import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "HTTP streaming export service (/services/export)"
    publishArtifacts = true
}

dependencies {
    implementation(project(":core:mill-spring-support"))
    implementation(project(":data:mill-data-autoconfigure"))
    implementation(project(":data:mill-data-backend-core"))
    implementation(project(":data:mill-data-source-core"))
    implementation(project(":services:mill-service-api"))
    implementation(libs.boot.starter.webmvc)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                }
            }
        }
    }
}
