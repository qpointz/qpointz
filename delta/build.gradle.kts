import java.nio.file.Files
import java.nio.file.Paths

plugins {
    base
    id("org.sonarqube") version "4.0.0.2929"
    id("jacoco-report-aggregation")
}

dependencies {
    //jacocoAggregation(project(":delta-jdbc-driver"))
    //jacocoAggregation(project(":delta-proto"))
    //jacocoAggregation(project(":delta-grpc-service"))
    jacocoAggregation(project(":delta-service-core"))
}



allprojects {
    fun getVersion():String {
        val path = Paths.get("${project.rootProject.projectDir}/../VERSION")
        if (!Files.exists(path)) {
            logger.warn("VERSION file missing {}:", path.toAbsolutePath().toString())
            return "0.0.1-NO-VERSION"
        }
        val version =  Files.readAllLines(path).get(0)
        logger.info("Version set to : {}", version)
        return version
    }
    group = "io.qpointz.delta"
    version = getVersion()
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType = TestSuiteType.UNIT_TEST
        }
    }
}

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}