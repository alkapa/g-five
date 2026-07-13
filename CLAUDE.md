# Círculo de Quintas Funcional — guía del proyecto

App Android (KMP + Compose) que enseña el círculo de quintas funcional.
Detalle de arquitectura y decisiones: `README.md` y `docs/REPORTE-EJECUCION.md`.

## Build y verificación

```bash
./gradlew -PdomainOnly=true :shared:jvmTest   # dominio puro (funciona sin Google Maven)
./gradlew :shared:testDebugUnitTest :androidApp:testDebugUnitTest :androidApp:assembleDebug
./gradlew -PenableOboe=true :androidApp:assembleDebug   # variante audio Oboe/NDK
```

En entornos sin acceso a `dl.google.com` (p. ej. el runner de Claude Code web)
solo funciona el modo `domainOnly`; la compilación completa se verifica en el
CI de GitHub Actions (se dispara con cada push a `claude/**`).

## Releases — REGLA DEL OWNER

**Cada cambio completado genera un release candidate sin preguntar.**
Procedimiento:

1. Subir la versión en `gradle.properties` (`VERSION_MINOR` para
   funcionalidad, `VERSION_PATCH` para fixes).
2. Hacer commit incluyendo la marca **`[rc]`** en el mensaje y pushear: el
   workflow `release-candidate.yml` valida (lint + tests), construye ambos
   APKs (AudioTrack y Oboe), los valida y publica el pre-release con tag
   `v{versión}-rc.{fecha}.{sha}` y los APKs adjuntos.
3. Verificar que el release quedó publicado y compartir la URL.

Notas: en ramas `claude/**` el RC solo corre con la marca `[rc]` (un RC por
cambio completado, no por cada push); `[skip ci]` salta todos los workflows.
Sin secrets `KEYSTORE_*` el RC publica el APK debug instalable; al
configurarlos, migrar a `assembleRelease` firmado (pipeline estilo Mimic).

## Convenciones

- El dominio musical vive en `:shared` (Kotlin puro) y SIEMPRE se verifica
  con tests jvm antes de pushear; la UI se verifica en CI.
- Contenido pedagógico en español neutro, sujeto a validación humana experta.
- CI/CD espejo de `alkapa/Mimic` (composite actions + versionado semántico
  en `gradle.properties`).
