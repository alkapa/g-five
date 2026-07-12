package com.alkapa.circuloquintas.domain

/**
 * Acorde dentro de una progresión: se guarda por GRADO + calidad, no por
 * nombre absoluto, para que el transporte de tonalidad funcione (§6.4).
 * [beats]: 1..8, default 4.
 */
data class ProgressionChord(
    val degreeIndex: Int,
    val quality: ChordQuality,
    val beats: Int = 4,
) {
    init {
        require(degreeIndex in 1..7) { "Grado fuera de rango: $degreeIndex" }
        require(beats in 1..8) { "Beats fuera de rango: $beats" }
    }
}

data class Progression(
    val id: Long?,
    val name: String,
    val key: Key,
    val bpm: Int,
    val chords: List<ProgressionChord>,
    val readOnly: Boolean = false,
) {

    /**
     * Transporte: cambia la tónica manteniendo grados; los acordes se
     * recalculan por grado (restricción §6.4).
     */
    fun transposeTo(newTonic: Note): Progression = copy(key = Key(newTonic, key.scale))

    /**
     * Renderiza los acordes concretos en la tonalidad actual. La fundamental
     * sale del grado en la escala activa y la calidad es la guardada — lo que
     * permite préstamos en las progresiones famosas (p. ej. el V MAYOR de la
     * cadencia andaluza sobre menor natural) sin mezclar escalas en el motor.
     */
    fun renderChords(): List<Chord> {
        val fieldKey = if (key.scale.category == ScaleCategory.PENTATONIC) key.parentKey()!! else key
        val scaleNotes = fieldKey.notes()
        return chords.map { pc ->
            ChordBuilder.build(scaleNotes[pc.degreeIndex - 1], pc.quality)
        }
    }
}
