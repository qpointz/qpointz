plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill Metadata service"
    publishArtifacts = false
}

dependencies {
    api(project(":metadata:mill-metadata-core"))
    implementation(project(":data:mill-data-autoconfigure"))
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.web)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.web)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.mockito.kotlin)
                }
            }
        }
    }
}
