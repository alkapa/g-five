package com.alkapa.circuloquintas.domain

/** Lente de análisis funcional (§4/§5 de la spec). Derivada de [ScaleType]. */
enum class Lens { TONAL, MODAL }

enum class ScaleCategory { TONAL, MODAL, PENTATONIC }

/**
 * Las 11 escalas de estudio de la v1 + los alias modales jónico/eólico.
 * [pattern]: semitonos entre grados consecutivos (§3.2); suma 12 siempre.
 * [modeIndex]: 1..7 para modos griegos (jónico = 1). Los modos se derivan por
 * rotación de la mayor madre (regla dura de la spec), el patrón queda como
 * dato de referencia cruzada para tests.
 */
enum class ScaleType(
    val pattern: List<Int>,
    val category: ScaleCategory,
    val modeIndex: Int? = null,
) {
    MAJOR(listOf(2, 2, 1, 2, 2, 2, 1), ScaleCategory.TONAL),
    NATURAL_MINOR(listOf(2, 1, 2, 2, 1, 2, 2), ScaleCategory.TONAL),
    HARMONIC_MINOR(listOf(2, 1, 2, 2, 1, 3, 1), ScaleCategory.TONAL),
    MELODIC_MINOR(listOf(2, 1, 2, 2, 2, 2, 1), ScaleCategory.TONAL),

    IONIAN(listOf(2, 2, 1, 2, 2, 2, 1), ScaleCategory.MODAL, 1),
    DORIAN(listOf(2, 1, 2, 2, 2, 1, 2), ScaleCategory.MODAL, 2),
    PHRYGIAN(listOf(1, 2, 2, 2, 1, 2, 2), ScaleCategory.MODAL, 3),
    LYDIAN(listOf(2, 2, 2, 1, 2, 2, 1), ScaleCategory.MODAL, 4),
    MIXOLYDIAN(listOf(2, 2, 1, 2, 2, 1, 2), ScaleCategory.MODAL, 5),
    AEOLIAN(listOf(2, 1, 2, 2, 1, 2, 2), ScaleCategory.MODAL, 6),
    LOCRIAN(listOf(1, 2, 2, 1, 2, 2, 2), ScaleCategory.MODAL, 7),

    PENT_MAJOR(listOf(2, 2, 3, 2, 3), ScaleCategory.PENTATONIC),
    PENT_MINOR(listOf(3, 2, 2, 3, 2), ScaleCategory.PENTATONIC),
    ;

    /**
     * Jónico y eólico se analizan con la lente tonal (§5.1); solo los cinco
     * modos restantes usan la lente modal. Las pentatónicas heredan la lente
     * tonal de su escala madre.
     */
    val lens: Lens
        get() = when (this) {
            DORIAN, PHRYGIAN, LYDIAN, MIXOLYDIAN, LOCRIAN -> Lens.MODAL
            else -> Lens.TONAL
        }

    val noteCount: Int get() = pattern.size

    /**
     * Escala cuyo contenido pedagógico de grados se reutiliza: jónico = mayor,
     * eólico = menor natural (§5.1: se tratan con lente tonal).
     */
    val pedagogicalAlias: ScaleType
        get() = when (this) {
            IONIAN -> MAJOR
            AEOLIAN -> NATURAL_MINOR
            else -> this
        }
}
