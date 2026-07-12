# Reporte final de ejecución — AI SPEC v2 «Círculo de Quintas Funcional»

Fecha: 2026-07-12 · Rama: `claude/circulo-quintas-android-tbpgwo`

> **Actualización (cierre de §12.7 vía GitHub Actions):** se agregó CI espejo
> del de `alkapa/Mimic` (`.github/workflows/ci.yml` + composite action
> `setup-android-build`). El run #2
> (<https://github.com/alkapa/g-five/actions/runs/29204077760>) terminó
> **completamente en verde**: `lintDebug` ✅, `:shared:testDebugUnitTest` +
> `:androidApp:testDebugUnitTest` ✅ (incluye DAO Room y ViewModels con
> Robolectric), `:androidApp:assembleDebug` ✅ (APK por defecto, AudioTrack) y
> `assembleDebug -PenableOboe=true` ✅ (APK con síntesis C++/Oboe; requirió el
> fix `-DANDROID_STL=c++_shared`, CXX1212). Artefactos: `debug-apk`,
> `debug-apk-oboe`, `lint-results`, `unit-test-results`. Con esto, el §3 de
> este reporte queda superado: la compilación y los tests de app corren en CI
> en cada push a `claude/**` y en PRs a `main`.

## 1. Unidades completadas (plan §10)

| # | Unidad | Estado | Verificación |
|---|--------|--------|--------------|
| 1 | Scaffold KMP + tema M3 + nav 2 tabs | ✅ | build de `:shared` verificado aquí; `:androidApp` escrito (ver §3) |
| 2 | Notas, grafía, 11 escalas, formatters | ✅ | tests en verde (jvmTest) |
| 3 | Campos armónicos + modos por rotación + cuatríadas | ✅ | tests en verde |
| 4 | Funciones tonal/modal + ambigüedad + `searchByNeed` | ✅ | tests en verde |
| 5 | Extensiones por regla + precaución | ✅ | tests en verde |
| 6 | Canvas del círculo (anillos, hit-testing, arco, tónica) | ✅ | código + revisión adversarial (no compilable aquí, §3) |
| 7 | Pantalla Círculo completa (VM, selectores, capas, barra, ficha) | ✅ | tests de VM escritos (ejecutables en entorno normal) |
| 8 | Contenido pedagógico (63+13 fichas, intro, precaución) | ✅ | test de cobertura + menciones obligatorias en verde |
| 9 | Audio Karplus-Strong + motores | ✅ | smoke tests de síntesis en verde; camino activo = fallback (§4) |
| 10 | Buscador por sensación (UI) | ✅ | código + revisión; lógica cubierta por tests de dominio |
| 11 | Constructor + playback + Room + biblioteca + transporte | ✅ | tests DAO/VM escritos (Robolectric; entorno normal) |
| 12 | Pulido (estado, accesibilidad, ajustes) + reporte | ✅ | este documento |

## 2. Tests ejecutados EN ESTE ENTORNO

`./gradlew -PdomainOnly=true :shared:jvmTest` → **71 tests, 0 fallos**, que cubren
todos los tests obligatorios de §11: escalas y grafía (incl. F## en G# menor
armónica y la no-mezcla de accidentes), campos armónicos (tríadas y cuatríadas,
4 tonales + modos por rotación), funciones tonales y modales con ambigüedad,
`searchByNeed` (REST→I primero; TENSION séptimas→G7 primero), extensiones
(Cadd9 sin precaución; 11ª natural sobre 3ª mayor → `caution=true`),
pentatónicas (notas, overlay 5/7, `parentKey`), formatters (americano/latino,
enarmonía Gb = 6 bemoles), progresiones (transporte C→G = G D Em C; 9 famosas
readOnly; andaluza = Am G F E con V mayor), las 24 tonalidades tonales + modos,
y smoke tests de síntesis (buffer no silente, sin NaN, decaimiento, mezcla sin
recorte).

Tests escritos pero **no ejecutables aquí** (requieren Google Maven):
`:androidApp:testDebugUnitTest` — DAO Room (siembra 9 famosas idempotente,
CRUD, orden de acordes, transporte) y ViewModels (interacción del círculo,
borrador, playback, solo-lectura de famosas, muerte de proceso).

## 3. Bloqueo del entorno de build (impacta criterios §12.7)

El proxy de este entorno **deniega `dl.google.com`** (y `maven.google.com`
redirige allí), además de los mirrors conocidos. Consecuencia: no se puede
descargar Android SDK, AGP ni ningún artefacto AndroidX/Compose/Room aquí, por
lo que `:androidApp:assembleDebug` y `:androidApp:testDebugUnitTest` **no
pudieron ejecutarse en este entorno** (no es un problema del proyecto sino de
la red del runner).

Mitigación aplicada (espíritu de la instrucción de resiliencia §8):

- Modo **`-PdomainOnly=true`**: excluye `:androidApp` y AGP; todo el motor de
  dominio + contenido + síntesis compila y se testea con Maven Central
  únicamente (así se verificó todo lo de §2 de este reporte).
- El código de `:androidApp` fue sometido a una **revisión adversarial
  multi-agente** (5 revisores por grupos de archivos contra las versiones
  fijadas + verificación adversarial de cada hallazgo): 2 hallazgos crudos,
  1 confirmado (desajuste de índices en la barra de escala con pentatónicas)
  y **corregido con test de regresión**; 1 refutado.
- En un entorno con acceso normal, la verificación completa es:
  `./gradlew :shared:testDebugUnitTest :androidApp:testDebugUnitTest :androidApp:assembleDebug`.

## 4. Camino de audio usado

**Fallback declarado (§8): `AudioTrackAudioEngine`** — la síntesis
Karplus-Strong vive en Kotlin común (`:shared`) con smoke tests en verde, y el
motor Android la sirve por AudioTrack en streaming. El camino **Oboe quedó en
el repo detrás de `-PenableOboe=true`** (C++ en `androidApp/src/main/cpp`,
puerto 1:1 de la síntesis, prefab + CMake), sin compilar aquí porque el NDK
tampoco es descargable. La app compila y suena por cualquiera de los dos
caminos, eligiendo en runtime (`AudioEngineFactory`).

## 5. [AMBIGUO] y [DECISIÓN-DEFAULT] aplicados

Defaults de la spec aplicados tal cual: `minSdk 24` / `targetSdk 35`;
Karplus-Strong; círculo fijo con arco resaltado y marcador de tónica; modos
con modelo de 3 categorías; tema claro/oscuro según sistema; nav 2 tabs +
hoja de ajustes; enarmonía default sostenidos con toggle (chip «↔ Gb» en la
cabecera + preferencia global en Ajustes); toque en anillo ext.→Mayor /
int.→Menor natural; voicing fundamental cerrada con tónica en 48-59; targets
iOS comentados con instrucciones en `shared/README.md`; violeta
`functionModalColor` para característicos modales.

Decisiones menores adicionales tomadas durante la implementación (veto
disponible):

1. **Celdas del círculo**: los acordes de familia mayor se muestran en el
   anillo exterior y los de familia menor en el interior (disposición clásica:
   en C mayor, F-C-G afuera y Dm-Am-Em-B° adentro; vii° ocupa la celda
   relativa de su pc).
2. **«Hacer tónica»** como botón en la ficha del grado (resuelve el conflicto
   de gestos entre «tocar acorde diatónico» y «elegir tónica diatónica»);
   la familia del acorde decide mayor/menor natural.
3. **Acorde `13` diatónico incluye la 11ª de la escala** y por tanto lleva
   etiqueta de precaución cuando la 11ª choca con la 3ª mayor (lectura de
   §3.5 «no ocultarlo»); la ficha explica que en la práctica se omite. En
   `m11`, si la escala da 9ª menor, esa 9ª se omite del voicing (el nombre no
   la implica). La plantilla de playback de `13` para acordes editados omite
   la 11ª (voicing práctico).
4. **Búsqueda COLOR**: extensiones sobre grados de función tónica (I primero)
   en lente tonal; característicos + extensiones del centro en lente modal.
   TENSION modal = acordes que contienen ambas notas del tritono de la madre.
5. **Borrador compartido**: la tonalidad del borrador la fija el primer
   acorde agregado; agregar sobre una famosa abierta (solo lectura) inicia
   un borrador nuevo. Reordenar = arrastre tras pulsación larga; eliminar =
   arrastre hacia arriba o botón en el diálogo de edición.
6. **Pentatónicas**: el arco del círculo también se dibuja para ellas (5
   quintas contiguas); desde el círculo solo suenan los grados incluidos en
   el overlay; la barra de escala muestra las 7 notas de la madre con 2
   atenuadas (§3.2).
7. Escala reproducida a 120 BPM fijos desde la barra (asc/desc).

## 6. Contenido pedagógico

Generado con fan-out multi-agente + doble revisión (rigor teórico y
menciones obligatorias/estilo) + pasada de corrección: 63 fichas de grado,
13 fichas de escala, intro del modelo funcional (declarado mapa pedagógico),
explicación de precaución y nota de tensión modal. Se corrigieron: 1 error
factual (notas compartidas de Em con G), ortografía sistemática en 4 bloques,
notación de disminuidos, matiz de la cadencia andaluza vs. frigio modal y los
apodos bII/bVII. **Pendiente obligatorio del PRD: validación por una persona
con formación musical formal antes del release.**

## 7. Pendientes / recomendaciones

1. Ejecutar en un entorno con Android SDK:
   `./gradlew :shared:testDebugUnitTest :androidApp:testDebugUnitTest :androidApp:assembleDebug`
   (criterio §12.7) y probar el APK en dispositivo (audio, gestos del
   constructor, rendimiento del canvas).
2. Compilar el camino Oboe con `-PenableOboe=true` cuando haya NDK.
3. Validación humana del contenido pedagógico (§1.3 del spec).
4. Accesibilidad: cada celda del círculo tiene objetivo táctil ≥48dp con
   `contentDescription`; conviene una pasada con TalkBack real.
5. El indicador «acorde sonando» cruza al tab Círculo vía `playbackHighlight`
   (implementado, no marcado pendiente).
