plugins {
    id ("org.sonarqube") version "5.0.0.4638"
    id("org.jetbrains.dokka")
    id("io.qpointz.plugins.mill-aggregate")
}

dependencies {
    dokka(project(":clients:mill-jdbc-driver"))
    dokka(project(":clients:mill-jdbc-shell"))
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