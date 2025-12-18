import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill AI testing library"
}

dependencies {
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jsr310)
    api(libs.jackson.datatype.jdk8)
    
    // JUnit Jupiter for ScenarioTestBase (testing library)
    api("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")
    api("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")

    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
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