plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill source core — storage abstraction, source model, descriptors"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-core"))
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.datatype.jdk8)
    compileOnly(libs.bundles.logging)
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
                    implementation(libs.slf4j.api)
                    implementation(libs.logback.core)
                    implementation(libs.logback.classic)
                }
            }
        }
    }
}
