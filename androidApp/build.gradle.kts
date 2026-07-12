import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Camino Oboe (NDK/C++): desactivado por defecto porque el entorno de build
// puede no disponer de NDK. Activar con `-PenableOboe=true`. Con el flag
// apagado, la app usa AudioTrackAudioEngine (misma síntesis Karplus-Strong en
// Kotlin) y NO compila código nativo. Ver §8 de la spec.
val enableOboe = providers.gradleProperty("enableOboe").orNull?.toBoolean() ?: false

// Versionado semántico desde gradle.properties (convención Mimic).
val versionMajor = (providers.gradleProperty("VERSION_MAJOR").orNull ?: "0").toInt()
val versionMinor = (providers.gradleProperty("VERSION_MINOR").orNull ?: "1").toInt()
val versionPatch = (providers.gradleProperty("VERSION_PATCH").orNull ?: "0").toInt()
val versionSuffix = providers.gradleProperty("VERSION_SUFFIX").orNull.orEmpty()

android {
    namespace = "com.alkapa.circuloquintas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alkapa.circuloquintas"
        minSdk = 24
        targetSdk = 35
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch$versionSuffix"
        buildConfigField("boolean", "ENABLE_OBOE", enableOboe.toString())
        if (enableOboe) {
            externalNativeBuild {
                cmake {
                    cppFlags += listOf("-std=c++17")
                    // El prefab de Oboe requiere la STL compartida (CXX1212).
                    arguments += listOf("-DANDROID_STL=c++_shared")
                }
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        if (enableOboe) {
            prefab = true
        }
    }

    if (enableOboe) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore.preferences)

    if (enableOboe) {
        implementation(libs.oboe)
    }

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
