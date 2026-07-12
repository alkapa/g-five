# Círculo de Quintas Funcional

App Android (100% offline, en español) que enseña el círculo de quintas con
lectura en tres capas — **acorde / grado / función armónica** — para
guitarristas autodidactas: todas las escalas de estudio, buscador de acordes
por sensación (Descanso · De paso · Tensión · Color), constructor de
progresiones con playback sintetizado y guardado local, cifrado dual
americano/latino y fichas pedagógicas.

## Módulos

- **`:shared`** — Kotlin Multiplatform (target activo `androidTarget()` + `jvm()`;
  iOS preparado pero desactivado, ver `shared/README.md`). Contiene el motor de
  teoría musical (notas con grafía correcta, 11 escalas, campos armónicos,
  funciones tonal/modal, extensiones por regla con etiqueta de precaución,
  transporte por grados), la síntesis Karplus-Strong, el contenido pedagógico
  y las interfaces (`AudioEngine`, repositorios).
- **`:androidApp`** — Jetpack Compose + Material 3, ViewModels con `StateFlow`,
  Room (progresiones + 9 famosas precargadas), DataStore (preferencias y
  última sesión) y los motores de audio.

## Compilar

```bash
./gradlew :androidApp:assembleDebug          # APK debug (requiere Android SDK)
./gradlew :shared:testDebugUnitTest \
          :androidApp:testDebugUnitTest \
          :androidApp:assembleDebug          # verificación completa
```

### Audio: dos caminos

- **Por defecto** (sin flags): `AudioTrackAudioEngine` — síntesis Karplus-Strong
  en Kotlin común sobre AudioTrack. No requiere NDK.
- **Oboe (NDK/C++)**: `./gradlew -PenableOboe=true :androidApp:assembleDebug` —
  misma síntesis portada a C++ sobre un stream Oboe de baja latencia
  (`androidApp/src/main/cpp`). Requiere NDK + CMake.

### Modo `domainOnly` (CI sin Android SDK / sin Google Maven)

```bash
./gradlew -PdomainOnly=true :shared:jvmTest
```

Excluye `:androidApp` y no aplica AGP: todo el motor de dominio y sus tests
(teoría musical §3-§5, contenido, síntesis) corren en cualquier JVM con acceso
solo a Maven Central. Es el modo con el que se desarrolló y verificó el motor
en un entorno sin acceso a `dl.google.com`.

## Decisiones clave

- El motor deriva los 7 modos griegos **por rotación** de la mayor madre, y las
  menores armónica/melódica elevando grados de la natural — sin tablas duplicadas.
- Las progresiones guardan **grado + calidad**, no nombres absolutos: el
  transporte de tonalidad recalcula los acordes por grado.
- El modelo de 3 funciones se presenta como **mapa pedagógico** (los grados
  ambiguos se marcan visualmente y su ficha lo explica); los modos usan una
  lente modal propia (centro / característico / neutro).
- Sin permiso `INTERNET` en el manifest; sin dependencias fuera de
  AndroidX/Compose/Room/DataStore (+ Oboe opcional).

El contenido pedagógico queda sujeto a validación por una persona con
formación musical formal antes del release.
