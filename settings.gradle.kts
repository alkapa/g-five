pluginManagement {
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
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "circulo-quintas"

include(":shared")

// Modo de verificación de dominio: `-PdomainOnly=true` omite :androidApp y todo
// rastro de AGP, de forma que el motor de dominio (:shared, target jvm) pueda
// compilarse y testearse en entornos sin acceso a Google Maven / Android SDK.
val domainOnly = providers.gradleProperty("domainOnly").orNull?.toBoolean() ?: false
if (!domainOnly) {
    include(":androidApp")
}
