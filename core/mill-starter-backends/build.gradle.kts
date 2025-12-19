plugins {
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    `java-library`
}

mill {
    description = "Library provides base Mill Service implementation"
    publishArtifacts = true
}


dependencies {
    api(project(":core:mill-service-core"))
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(libs.calcite.file)
    implementation(libs.boot.starter)
    implementation(libs.substrait.isthmus)
    compileOnly(libs.lombok)
    runtimeOnly(libs.bundles.logging)
    runtimeOnly(libs.h2.database)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)
    testImplementation(libs.boot.starter.test)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
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
