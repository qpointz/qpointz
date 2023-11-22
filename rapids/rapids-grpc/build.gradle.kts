plugins {
    `java-library`
    id("com.google.protobuf") version "0.9.4"
    jacoco
}

buildscript {
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}

dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.bundles.logging)

    api(libs.vertx.grpc)
    api(libs.vertx.grpc.server)
    api(libs.vertx.grpc.client)
    api(libs.protobuf.java)
    implementation(libs.javax.annotation.api)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
        }
        create("vertx") {
            artifact = libs.vertx.grpc.protoc.plugin2.get().toString()
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins{
                create("grpc")
                create("vertx")
            }
        }
    }
}


testing {
    suites {
        register<JvmTestSuite>("testIT") {
            testType.set(TestSuiteType.INTEGRATION_TEST)
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}