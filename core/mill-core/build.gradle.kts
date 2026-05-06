plugins {
    `java-library`
    alias(libs.plugins.google.protobuf.plugin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    publishArtifacts = true
    description = "Mill core classes and interfaces."
}

buildscript {
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:${libs.versions.protobuf.plugin.get()}")
    }
}

// Maven classifiers for published `protoc` / `protoc-gen-grpc-java` executables (not `System.getProperty("osdetector.classifier")`, which is unset).
fun protoNativeClassifier(): String {
    val osName = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    val isArm = arch.contains("aarch") || arch == "arm" || arch == "arm64" || arch.contains("arm64")
    return when {
        osName.contains("windows") -> if (isArm) "windows-aarch_64" else "windows-x86_64"
        osName.contains("mac") -> if (isArm) "osx-aarch_64" else "osx-x86_64"
        else -> when {
            isArm -> "linux-aarch_64"
            arch.contains("ppc") -> "linux-ppcle_64"
            else -> "linux-x86_64"
        }
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
    api(libs.jakarta.annotation.api)

    api(libs.bundles.jackson)
    implementation(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

protobuf {
    protoc {
        // Gradle 10 deprecates "multi-string" dependency notation used by older protobuf-gradle-plugin
        // forms. Use single-string with classifier + @exe.
        val osClassifier = protoNativeClassifier()
        val protobufVersion = libs.protobuf.protoc.get().toString().substringAfterLast(':')
        artifact = "com.google.protobuf:protoc:$protobufVersion:$osClassifier@exe"
    }
    plugins {
        create("grpc") {
            val osClassifier = protoNativeClassifier()
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}:$osClassifier@exe"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
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
