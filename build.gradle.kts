import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.gradle.dependency.update)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.spotless)
    alias(libs.plugins.version.catalog.update)
}

group = "ai.basemind"
version = "1.0.0"

ext {
    set("sdkArtifactId", "client")
}

versionCatalogUpdate {
    sortByKey.set(true)
    pin {}
    keep {
        keepUnusedVersions.set(false)
        keepUnusedLibraries.set(false)
        keepUnusedPlugins.set(false)
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = true
    checkConstraints = true
    checkBuildEnvironmentConstraints = true
    gradleReleaseChannel = "current"
    reportfileName = "report"
}

/**
 * isNonStable checks if a given version is not stable
 */
fun isStableVersion(version: String): Boolean {
    val containsNonStableVersionKeyword = listOf("alpha", "beta", "rc").any { version.lowercase().contains(it) }

    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return !containsNonStableVersionKeyword && regex.matches(version)
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isStableVersion(currentVersion) && !isStableVersion(candidate.version)
    }
}

spotless {
    java {
        target("**/*.java")
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
    }
    kotlin {
        target("**/*.kt")
        ktfmt()
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_function-naming" to "disabled",
                "ktlint_standard_no-wildcard-imports" to "disabled",
            ),
        )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktfmt()
        ktlint()
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            username.set(System.getenv("SONATYPE_USER"))
        }
    }
    repositoryDescription = "$group:${project.ext.get("sdkArtifactId")}:$version"
}
