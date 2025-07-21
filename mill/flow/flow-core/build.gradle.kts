plugins {
    `java-library`
    id("io.qpointz.plugins.mill")
}

mill {
    publishToSonatype = true
    description = "Flow core module, which provides the core functionality for the Flow framework."
}

dependencies {
    api("io.qpointz.mill:mill-core")
    /*api(libs.substrait.core)
    api(libs.jackson.databind)
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.datatype.jsr310)
    api(libs.jackson.datatype.jdk8)
    api(libs.grpc.netty.shaded)
    api(libs.grpc.protobuf)
    api(libs.grpc.stub)
    api(libs.grpc.inprocess)
    api(libs.javax.annotation.api)*/

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
                    annotationProcessor(libs.lombok)
                    compileOnly(libs.lombok)
                }
            }
        }
    }
}