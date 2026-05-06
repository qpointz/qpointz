import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "JSON array export encoder (SPI for mill-export-service)"
    publishArtifacts = true
}

dependencies {
    api(project(":data:mill-data-source-core"))
    implementation(project(":core:mill-core"))
    implementation(libs.bundles.jackson)
    compileOnly(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.assertj.core)
                }
            }
        }
    }
}
