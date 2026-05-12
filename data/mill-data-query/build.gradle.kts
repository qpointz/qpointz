import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Query result execution sessions (Spring-free core)"
    publishArtifacts = true
}

dependencies {
    api(project(":data:mill-data-backend-core"))
    implementation(project(":core:mill-core"))
    implementation(libs.caffeine)
    implementation(libs.jackson.databind)
    implementation(libs.bundles.logging)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.junit.jupiter.api)
                }
            }
        }
    }
}
