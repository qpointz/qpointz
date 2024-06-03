plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.5"
    jacoco
}

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
    implementation("com.google.api.grpc:proto-google-common-protos:2.29.0")
    implementation(libs.grpc.testing)
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
                    implementation(libs.bootGRPC.client)
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
