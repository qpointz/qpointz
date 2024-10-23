plugins {
    base
    id("jacoco-report-aggregation")
    id ("org.sonarqube") version "5.0.0.4638"
    java
}

sonar {
    properties {
        property("sonar.projectKey", "qpointz-delta")
        property("sonar.projectName", "qpointz-delta")
        property("sonar.qualitygate.wait", true)
    }
}

tasks.register<Zip>("publishSonatypeBundle") {
    description = "Zips Sonatype Bundle to be published to Maven Central Repository"
    group = "publishing"
    from(layout.buildDirectory.dir("repo"))
    include ("**/*")
    archiveBaseName.set("sonatype-bundle")
    destinationDirectory.set(layout.buildDirectory.dir("sonatype-bundle"))
}

/* reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
            reportTask.get().classDirectories.setFrom(reportTask.get().classDirectories.map {
                fileTree(it).matching {
                    exclude(listOf("proto/**"))
                }
            })
        }
    }
} */