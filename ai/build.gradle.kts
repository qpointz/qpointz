plugins {
    base
    id("jacoco-report-aggregation")
}

tasks.register("testIT") {
    description = "Runs all testIT tasks across all subprojects"
    group = "verification"

    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("testIT")
        }
    )
}

tasks.register("test") {
    description = "Runs all testIT tasks across all subprojects"
    group = "verification"

    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("test")
        }
    )
}