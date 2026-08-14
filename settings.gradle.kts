pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PeriodFlow"

// App module
include(":app")

// Core modules
include(":core:common")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":core:ui")
include(":core:health-analysis")
include(":core:security")
include(":core:export")
include(":core:notifications")
include(":core:network")
include(":core:ai")

// Feature modules
include(":feature:home")
include(":feature:log")
include(":feature:stats")
include(":feature:settings")
include(":feature:health-insights")
include(":feature:onboarding")
