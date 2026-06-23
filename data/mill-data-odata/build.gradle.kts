import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "OData v4 EDM and query composition (Spring-free core)"
    publishArtifacts = true
}

dependencies {
    api(project(":data:mill-data-backend-core"))
    implementation(project(":data:mill-data-schema-core"))
    implementation(project(":data:mill-data-backends"))
    implementation(libs.calcite.core)
    implementation(libs.sdl.odata.api)
    implementation(libs.sdl.odata.edm)
    implementation(libs.sdl.odata.parser)
    implementation(libs.jackson.databind)
    implementation(libs.bundles.logging)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.mockito.kotlin)
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
