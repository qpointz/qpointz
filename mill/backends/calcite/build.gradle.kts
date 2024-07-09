import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    application
    java
    id("org.springframework.boot") version libs.versions.boot
    id("io.spring.dependency-management") version "1.1.4"
    jacoco
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

application {
    applicationName = "calcite-backend-service"
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

dependencies {
    implementation(project(":backends:backend-core"))
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(libs.calcite.file)

    runtimeOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.substrait.isthmus)

    implementation(libs.bootGRPC.server)
    implementation(libs.bootGRPC.client)
    developmentOnly(libs.boot.devtools)
    annotationProcessor(libs.boot.configuration.processor)
    testImplementation(libs.boot.starter.test)
    testImplementation("io.projectreactor:reactor-test")
}

springBoot {
    mainClass = "io.qpointz.delta.service.CalciteDeltaService"
}

application {
    mainClass = "io.qpointz.delta.service.CalciteDeltaService"
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
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
                    //implementation(libs.h2.database)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
