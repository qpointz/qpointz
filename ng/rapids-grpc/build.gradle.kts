import com.google.protobuf.gradle.*

plugins {
    `java-library`
    jacoco
    id("jacoco-report-aggregation")
    id("com.google.protobuf") version "0.9.4"
}

buildscript {
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.vertx.grpc.server)
    implementation(libs.vertx.grpc.client)
    implementation ("javax.annotation:javax.annotation-api:1.3.2")
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.bundles.logging)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.2.0"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
        }
        id("vertx") {
            artifact = "io.vertx:vertx-grpc-protoc-plugin2:4.4.5"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") { }
                id("vertx") { }
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
                    implementation(project(":rapids-testkit"))

                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)

                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}