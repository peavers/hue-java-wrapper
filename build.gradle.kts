plugins {
    id("com.github.ben-manes.versions") version "0.51.0"
    id("nl.littlerobots.version-catalog-update") version "0.8.4"
}

versionCatalogUpdate {
    sortByKey.set(true)
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}