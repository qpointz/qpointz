plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill metadata core library"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-core"))
    implementation(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-schema-core"))
                    implementation(libs.assertj.core)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.mockito.kotlin)
                    implementation(libs.h2.database)
                }
            }
        }
    }
}
