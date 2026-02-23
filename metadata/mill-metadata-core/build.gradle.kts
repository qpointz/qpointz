plugins {
    `java-library`
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill metadata core library"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-core"))
    api(libs.bundles.jackson)
    implementation(libs.json.schema.validator)
    implementation(libs.bundles.logging)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())
            
            dependencies {
                implementation(project())
                implementation(project(":metadata:mill-metadata-autoconfigure"))
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.web)
                implementation(libs.lombok)
                annotationProcessor(libs.lombok)
            }
        }

        configureEach {
            if (this is JvmTestSuite && this.name != "testIT") {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
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
