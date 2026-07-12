# :shared — dominio KMP

Módulo Kotlin Multiplatform con el motor de teoría musical, el contenido
pedagógico y las interfaces (`AudioEngine`, repositorios). Kotlin puro, sin
dependencias de plataforma.

## Targets

- `androidTarget()` — activo (consumido por `:androidApp`).
- `jvm()` — activo; permite ejecutar todos los tests de dominio en cualquier
  JVM sin Android SDK: `./gradlew -PdomainOnly=true :shared:jvmTest`.
- iOS — **preparado pero desactivado** en esta release.

## Activar targets iOS

1. En `shared/build.gradle.kts`, descomentar `iosX64()`, `iosArm64()` y
   `iosSimulatorArm64()` dentro del bloque `kotlin { }`.
2. Compilar desde macOS con Xcode instalado.
3. Aportar una implementación iOS de `AudioEngine` (Oboe es solo-Android; la
   síntesis Karplus-Strong de `domain/audio/KarplusStrongSynth.kt` es Kotlin
   común y puede reutilizarse sobre AVAudioEngine/AudioUnit).

## Modo `domainOnly`

`-PdomainOnly=true` excluye `:androidApp` del build y no aplica AGP en este
módulo (queda solo el target `jvm`). Sirve para verificar el dominio en
entornos sin acceso a Google Maven / Android SDK (CI restringida).
