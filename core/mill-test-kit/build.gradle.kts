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
    api(project(":core:mill-security-core"))
    api(project(":core:mill-service-core"))
    api(project(":data:mill-data-backends"))
    api(libs.boot.starter)
    api(libs.boot.starter.web)
    api(libs.boot.starter.test)
    api(libs.mockito.core)
    api(libs.mockito.junit.jupiter)
    api(libs.jackson.core)
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jsr310)
    api(libs.jackson.datatype.jdk8)
    api(libs.javax.annotation.api)
    api("no.nav.security:mock-oauth2-server:3.0.1")
    api(libs.h2.database)
    // JUnit Jupiter for ScenarioTestBase (testing library)
    api("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")
    api("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)

    annotationProcessor(libs.boot.configuration.processor)
    annotationProcessor(libs.lombok)
}

tasks.named<Javadoc>("javadoc") { //temporary
    isFailOnError = false
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.boot.starter.test)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
