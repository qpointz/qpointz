plugins {
    `java-library`
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill metadata provider — legacy metadata interfaces and models"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-core"))
    api(project(":metadata:mill-metadata-core"))
    api(project(":data:mill-data-backend-core"))
    implementation(libs.bundles.jackson)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-testkit"))
                    implementation(project(":data:mill-data-backends"))
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.h2.database)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
