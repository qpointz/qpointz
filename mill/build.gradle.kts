import java.nio.file.Files
import java.nio.file.Paths

plugins {
    base
    id("jacoco-report-aggregation")
    id ("org.sonarqube") version "5.0.0.4638"
}

sonar {
    properties {
        property("sonar.projectKey", "qpointz-delta")
        property("sonar.projectName", "qpointz-delta")
        property("sonar.qualitygate.wait", true)        
    }
}

dependencies {
    jacocoAggregation(project(":core"))
    jacocoAggregation(project(":backends:backend-core"))
    jacocoAggregation(project(":backends:calcite"))
    jacocoAggregation(project(":services:auth-service"))
}

allprojects {
    fun getVersion():String {
        val path = Paths.get("${project.rootProject.projectDir}/../VERSION")
        if (!Files.exists(path)) {
            logger.trace("VERSION file missing {}:", path.toAbsolutePath().toString())
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