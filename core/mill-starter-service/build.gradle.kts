plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill essential starter services"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-service-core"))
    api(project(":core:mill-security-core"))

    api(libs.boot.starter)
    api(libs.boot.starter.web)
    api(libs.boot.starter.security)
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.datatype.jsr310)

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
                    implementation(project(":core:mill-starter-backends"))
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
