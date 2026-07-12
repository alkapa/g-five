import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

// En modo domainOnly no se aplica AGP: solo queda el target jvm, suficiente
// para compilar y testear todo el dominio (commonMain/commonTest) sin Google Maven.
val domainOnly = providers.gradleProperty("domainOnly").orNull?.toBoolean() ?: false

if (!domainOnly) {
    apply(plugin = "com.android.library")
    // La configuración del bloque `android {}` vive en un script Groovy aparte
    // para que este .kts no referencie tipos de AGP en tiempo de compilación
    // del script (imprescindible para que el modo domainOnly funcione).
    apply(from = "android-library.gradle")
}

kotlin {
    jvm()

    if (!domainOnly) {
        androidTarget {
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions {
                        (this as org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions).jvmTarget.set(JvmTarget.JVM_17)
                    }
                }
            }
        }
    }

    // Targets iOS preparados pero NO activos en esta release (ver README.md de
    // este módulo): el entorno de build puede no tener toolchain de Apple.
    // iosX64()
    // iosArm64()
    // iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
