plugins {
    `java-library`
    id("io.qpointz.plugins.mill")
}

mill {
    publishArtifacts = true
    description = "Flow module, which provides the Excell integration for the Flow framework."
}

dependencies {
    implementation(project(":flow-core"))
    implementation(libs.apache.poi)
    implementation(libs.apache.poi.ooxml)
    implementation(libs.bundles.logging)
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
                    implementation(libs.lombok)
                    runtimeOnly(libs.apache.poi.ooxml)
                    annotationProcessor(libs.lombok)
                    compileOnly(libs.lombok)
                }
            }
        }
    }
}