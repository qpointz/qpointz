plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.4"
    id("com.google.protobuf") version "0.9.4"
}

val projectName = "lala2"
val projectDescription = "Lala2"

//properties["pom.name"] = "lala2"

buildscript {
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}

sourceSets {
    main {
        proto {
            srcDir("../proto")
            exclude("substrait/**")
        }
    }
}

tasks.register<Sync>("copyResources") {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("extraResources1"))
}



dependencies {
    api(libs.substrait.core)
    implementation(libs.bundles.logging)
    api(libs.grpc.protobuf)
    api(libs.grpc.stub)
    api(libs.javax.annotation.api)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.64.0"
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
        register<JvmTestSuite>("testIT") {
            testType.set(TestSuiteType.INTEGRATION_TEST)
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
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