plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description =
        "Shared data-layer metadata — MetadataSource implementations, relational entity paths, and in-code facet manifests for data modules"
    publishArtifacts = true
}

dependencies {
    api(project(":metadata:mill-metadata-core"))
    api(project(":data:mill-data-backend-core"))
    implementation(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.assertj.core)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.mockito.kotlin)
                }
            }
        }
    }
}
