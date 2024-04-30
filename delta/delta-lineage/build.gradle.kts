import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version libs.versions.boot
    id("io.spring.dependency-management") version "1.1.4"
    jacoco
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

dependencies {
    implementation(libs.boot.starter)
    implementation(libs.calcite.core)
    implementation(libs.calcite.server)
    implementation(libs.calcite.csv)
    implementation(libs.calcite.file)

    implementation(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.substrait.isthmus)
    developmentOnly(libs.boot.devtools)
    annotationProcessor(libs.boot.configuration.processor)
    testImplementation(libs.boot.starter.test)
}

springBoot {
    //mainClass = "io.qpointz.delta.calcite.CalciteDeltaService"
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
