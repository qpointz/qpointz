plugins {
    `java-library`
    alias(libs.plugins.google.protobuf.plugin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    publishArtifacts = true
    description = "Library provides common mill's classes"
}

buildscript {
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:${libs.versions.protobuf.plugin.get()}")
    }
}

sourceSets {
    main {
        proto {
            srcDir("../../proto")
            exclude("substrait/**")
        }
    }
}

dependencies {
    api(libs.substrait.core)
    api(libs.grpc.netty.shaded)
    api(libs.grpc.protobuf)
    api(libs.protobuf.java)
    api(libs.grpc.stub)
    api(libs.grpc.inprocess)
    api(libs.javax.annotation.api)

    implementation(libs.bundles.jackson)
    implementation(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins{
                create("grpc") {
                    ofSourceSet("main")
                }
            }
        }
    }
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.grpc.netty.shaded)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    implementation(libs.h2.database)
                    annotationProcessor(libs.lombok)
                    compileOnly(libs.lombok)
                }
            }
        }
    }
}