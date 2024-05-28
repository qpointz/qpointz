plugins {
    java
    application
    jacoco
    id("org.graalvm.buildtools.native").version("0.10.2")
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

    implementation("org.commonmark:commonmark:0.22.0")
    implementation("org.jsoup:jsoup:1.17.2")

    implementation("info.picocli:picocli:4.7.6")
    implementation(libs.calcite.core)
    implementation(libs.calcite.server)
    implementation(libs.calcite.csv)
    implementation(libs.calcite.file)
    implementation("io.openlineage:openlineage-java:1.16.0")


    implementation(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")
}

graalvmNative {
    toolchainDetection = true
}

application {
    mainClass = "io.qpointz.delta.lineage.Entry"
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
