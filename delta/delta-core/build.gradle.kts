plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.4"
    id("com.google.protobuf") version "0.9.4"
    jacoco
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

shadow {
    //baseN
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}

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

        resources {
            srcDir("../delta-ui/build")
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
    testImplementation(libs.h2.database)
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
