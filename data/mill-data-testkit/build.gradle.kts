plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Library provides base Mill Service implementation"
    publishArtifacts = true
}


dependencies {
    implementation(project(":data:mill-data-backend-core"))
    implementation(project(":data:mill-data-backends"))
    implementation(project(":data:mill-data-source-calcite"))
    implementation(libs.calcite.core)

//    implementation(libs.calcite.core)
//    implementation(libs.calcite.csv)
//    implementation(libs.calcite.file)
//    implementation(libs.substrait.isthmus)
    compileOnly(libs.lombok)
    runtimeOnly(libs.bundles.logging)
    annotationProcessor(libs.lombok)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-autoconfigure"))
                    implementation(project(":data:formats:mill-data-format-text"))
                    implementation(libs.boot.starter)
                    //annotationProcessor(libs.boot.configuration.processor)
                    implementation(libs.boot.starter.test)
                    implementation(libs.calcite.core)
                    implementation(libs.protobuf.java.util)
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
