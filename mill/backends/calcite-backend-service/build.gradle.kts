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


springBoot {
    mainClass = "io.qpointz.mill.service.CalciteMillService"
}

application {
    mainClass = springBoot.mainClass
    applicationName = "calcite-backend-service"
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

tasks.getByName("installDist").doLast {
    val distName = distributions.getByName("main").distributionBaseName.get()
    val outdir = project.layout.buildDirectory.dir("install/${distName}").get()

    copy {
        from(layout.projectDirectory.dir("etc/config/default"))
        into(outdir.dir("config"))
    }
    copy {
        from(rootProject.layout.projectDirectory.dir("../etc/data/datasets/airlines/csv"))
        into(outdir.dir("examples/data/airlines"))
    }
}

dependencies {
    implementation(project(":backend-service-core"))
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
