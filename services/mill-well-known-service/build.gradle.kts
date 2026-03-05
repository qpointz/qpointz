plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill shared service starter"
    publishArtifacts = true
}

dependencies {
    api(project(":services:mill-service-api"))
    api(project(":services:mill-service-security"))
    api(project(":data:mill-data-backend-core"))

    api(libs.boot.starter)
    api(libs.boot.starter.web)
    api(libs.boot.starter.security)
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.datatype.jsr310)

    implementation(project(":core:mill-spring-support"))

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
                    implementation(project(":data:mill-data-backends"))
                    implementation(project(":data:mill-data-autoconfigure"))
                    implementation(libs.spring.security.test)
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.web)
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
