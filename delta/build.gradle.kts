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
    jacocoAggregation(project(":delta-core"))
    jacocoAggregation(project(":delta-service-core"))
    jacocoAggregation(project(":delta-service-calcite"))
    jacocoAggregation(project(":rapids-navigator-api"))
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