plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill testing kit"
    publishArtifacts = true
}

dependencies {
    api("io.qpointz.mill:mill-security-core")
    api(project(":mill-service-core"))
    api(project(":mill-starter-backends"))
    api(libs.boot.starter)
    api(libs.boot.starter.web)
    api(libs.boot.starter.test)
    api(libs.mockito.core)
    api(libs.mockito.junit.jupiter)
    api(libs.jackson.core)
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.datatype.jsr310)
    api(libs.javax.annotation.api)
    api("no.nav.security:mock-oauth2-server:3.0.1")
    api(libs.h2.database)
    compileOnly(libs.lombok)

    annotationProcessor(libs.boot.configuration.processor)
    annotationProcessor(libs.lombok)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                }
            }
        }
    }
}
