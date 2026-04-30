plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill service security — Spring Security authentication and wiring"
    publishArtifacts = true
}

dependencies {
    api(project(":security:mill-security"))

    api(project(":services:mill-service-api"))
    api(libs.boot.starter.security)

    implementation(project(":core:mill-spring-support"))
    implementation(project(":core:mill-core"))
    implementation(project(":core:mill-spring-support"))
    implementation(libs.boot.starter.security.oauth2.resource.server)
    implementation(libs.boot.starter.security.oauth2.client)
    implementation(libs.boot.starter.webmvc)
    implementation(libs.okhttp)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)

    compileOnly(libs.lombok)

    runtimeOnly(libs.bundles.logging)

    testImplementation(libs.boot.starter.test)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":core:mill-test-kit"))
                    implementation(project(":data:mill-data-autoconfigure"))
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.restclient.test)
                    implementation(libs.boot.resttestclient)
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
