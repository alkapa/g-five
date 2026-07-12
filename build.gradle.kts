// Raíz. Los plugins se cargan por classpath de buildscript (en lugar del
// bloque `plugins {}` estático) para poder condicionar AGP: en modo
// `-PdomainOnly=true` no se toca Google Maven y el dominio (:shared, jvm)
// compila y testea en entornos sin acceso a repositorios de Google.
buildscript {
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
    dependencies {
        classpath(libs.kotlin.gradlePlugin)
        val domainOnly = providers.gradleProperty("domainOnly").orNull?.toBoolean() ?: false
        if (!domainOnly) {
            classpath(libs.android.gradlePlugin)
            classpath(libs.compose.compilerGradlePlugin)
            classpath(libs.ksp.gradlePlugin)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
