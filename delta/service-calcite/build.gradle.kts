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

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

dependencies {
    implementation(project(":service-core"))
    implementation(project(":core"))
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

    //implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
	//implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	//implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	//implementation("org.springframework.boot:spring-boot-starter-webflux")
	//implementation("org.springframework.session:spring-session-core")
	//developmentOnly("org.springframework.boot:spring-boot-docker-compose")

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
