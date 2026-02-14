plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill source format — Avro"
    publishArtifacts = true
}

dependencies {
    api(project(":source:mill-source-core"))
    implementation(libs.apache.avro)
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
