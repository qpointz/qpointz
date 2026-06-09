plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Analysis domain contract (saved queries port and types; no persistence framework)"
    publishArtifacts = true
}

dependencies {
    implementation(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is org.gradle.api.plugins.jvm.JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.junit.jupiter.api)
                    implementation(libs.assertj.core)
                }
            }
        }
    }
}
