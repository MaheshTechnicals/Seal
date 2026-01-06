pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
    id("com.gradle.develocity") version("3.19")
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        
        // Publish to scans.gradle.com
        publishing.onlyIf { true }
        
        // Add build metadata
        tag(if (System.getenv("CI") != null) "CI" else "Local")
        
        // Capture additional info in CI
        if (System.getenv("CI") != null) {
            tag("GitHub-Actions")
            System.getenv("GITHUB_RUN_ID")?.let { value("GitHub Run ID", it) }
            System.getenv("GITHUB_REF_NAME")?.let { value("Git Branch", it) }
        }
    }
}

rootProject.name = "Seal Plus"
include (":app")
include(":color")