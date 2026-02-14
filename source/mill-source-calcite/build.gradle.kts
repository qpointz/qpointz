plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill source Calcite adapter"
    publishArtifacts = true
}

dependencies {
    api(project(":source:mill-source-core"))
    api(project(":core:mill-core"))
    implementation(libs.calcite.core)
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
