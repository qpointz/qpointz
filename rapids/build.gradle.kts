plugins {
    base
    id("org.sonarqube") version "4.0.0.2929"
    id("jacoco-report-aggregation")
}

dependencies {
    jacocoAggregation(project(":rapids-common"))
    jacocoAggregation(project(":rapids-core-legacy"))
    jacocoAggregation(project(":rapids-jdbc-driver"))
    jacocoAggregation(project(":rapids-srv-worker"))
    jacocoAggregation(project(":rapids-grpc"))
    jacocoAggregation(project(":rapids-grpc-service"))
}



allprojects {
    group = "io.qpointz.rapids"
    version = "1.0.0-SNAPSHOT"
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