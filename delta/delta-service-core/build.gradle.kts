plugins {
    `java-library`
    //id("org.springframework.boot") version libs.versions.boot
    id("io.spring.dependency-management") version "1.1.5"
    //id("com.google.protobuf") version "0.9.4"
    jacoco
}

//buildscript {
//    dependencies {
//        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
//    }
//}

//sourceSets {
//    main {
//        proto {
//            srcDir("../proto")
//            exclude("substrait/**")
//        }
//
//        resources {
//            srcDir("../delta-ui/build")
//        }
//    }
//}


tasks.register<Copy>("copyUI") {
    from(layout.projectDirectory.dir("../delta-ui/build"))
    into(layout.projectDirectory.dir("/src/main/resources/ui"))
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
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

dependencies {
    implementation(project(":delta-core"))

    implementation(libs.javax.annotation.api)
    api(libs.boot.starter.security)
    api(libs.grpc.core)
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.bootGRPC.server)

    //developmentOnly(libs.boot.devtools)
    //annotationProcessor(libs.boot.configuration.processor)
    //testImplementation(libs.boot.starter.test)
    //testImplementation("io.projectreactor:reactor-test:${libs.versions.boot.get()}")

    //implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
	//implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	//implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	//implementation("org.springframework.boot:spring-boot-starter-webflux")
	//implementation("org.springframework.session:spring-session-core")
	//developmentOnly("org.springframework.boot:spring-boot-docker-compose")
}

//protobuf {
//    protoc {
//        artifact = libs.protobuf.protoc.get().toString()
//    }
//    plugins {
//        create("grpc") {
//            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
//        }
//    }
//    generateProtoTasks {
//        all().forEach {
//            it.plugins{
//                create("grpc") {
//                    ofSourceSet("main")
//                }
//            }
//        }
//    }
//}

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
                    implementation(libs.h2.database)
                    implementation(libs.calcite.core)
                    implementation(libs.calcite.file)
                    implementation(libs.calcite.csv)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
