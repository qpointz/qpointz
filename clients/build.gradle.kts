plugins {
    id ("org.sonarqube") version "5.0.0.4638"
}

sonar {
    properties {
        property(
            "sonar.sources",
            listOf(project.layout.projectDirectory.dir("mill-py/millclient"))
        )
        property(
            "sonar.tests",
            listOf(project.layout.projectDirectory.dir("mill-py/tests"))
        )
    }
}