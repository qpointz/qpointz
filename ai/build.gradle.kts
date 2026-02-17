plugins {
    base
    id("jacoco-report-aggregation")
    id("io.qpointz.plugins.mill-aggregate")
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":ai:mill-ai-v1-core"))
    dokka(project(":ai:mill-ai-v1-nlsql-chat-service"))
    dokka(project(":ai:mill-ai-v2"))
    dokka(project(":ai:mill-ai-v2-test"))
}
