package com.alkapa.circuloquintas.domain

/**
 * Overlay pentatónico (§3.2): la pentatónica no tiene campo armónico propio;
 * se muestra sobre su tonalidad madre con [includedDegrees] resaltados (1..7)
 * y los otros dos grados atenuados.
 */
data class PentatonicOverlay(val parent: Key, val includedDegrees: Set<Int>)

/**
 * Tonalidad: tónica + escala. Punto de entrada del motor de dominio.
 */
data class Key(val tonic: Note, val scale: ScaleType) {

    /** Notas de la escala con grafía correcta (§3.1-§3.2): una letra por grado. */
    fun notes(): List<Note> = when (scale.category) {
        ScaleCategory.PENTATONIC -> {
            val overlay = pentatonicOverlay()!!
            val parentNotes = overlay.parent.notes()
            overlay.includedDegrees.sorted().map { parentNotes[it - 1] }
        }
        ScaleCategory.MODAL -> modeNotesByRotation()
        ScaleCategory.TONAL -> tonalNotes()
    }

    /**
     * Escala madre: pentatónicas → mayor / menor natural de la misma tónica;
     * modos → la mayor cuya rotación produce el modo (D dórico → C mayor).
     * Escalas tonales → null.
     */
    fun parentKey(): Key? = when {
        scale == ScaleType.PENT_MAJOR -> Key(tonic, ScaleType.MAJOR)
        scale == ScaleType.PENT_MINOR -> Key(tonic, ScaleType.NATURAL_MINOR)
        scale.modeIndex != null -> parentMajor()
        else -> null
    }

    /** Lente de análisis activa (§4/§5). Las pentatónicas usan la lente tonal de su madre. */
    val lens: Lens get() = scale.lens

    /** Nota característica del modo (§5.1); null fuera de la lente modal. */
    fun characteristicNote(): Note? =
        CHARACTERISTIC_DEGREE[scale]?.let { notes()[it - 1] }

    fun pentatonicOverlay(): PentatonicOverlay? = when (scale) {
        // Grados de la madre presentes en la pentatónica: mayor 1-2-3-5-6, menor 1-3-4-5-7.
        ScaleType.PENT_MAJOR -> PentatonicOverlay(Key(tonic, ScaleType.MAJOR), setOf(1, 2, 3, 5, 6))
        ScaleType.PENT_MINOR -> PentatonicOverlay(Key(tonic, ScaleType.NATURAL_MINOR), setOf(1, 3, 4, 5, 7))
        else -> null
    }

    /**
     * Campo armónico diatónico (§3.3-§3.5). Las pentatónicas devuelven el
     * campo de su escala madre (§3.2: no tienen campo propio).
     */
    fun diatonicField(level: ChordLevel): List<Degree> {
        if (scale.category == ScaleCategory.PENTATONIC) {
            return parentKey()!!.diatonicField(level)
        }
        val sn = notes()
        val charNote = CHARACTERISTIC_DEGREE[scale]?.let { sn[it - 1] }
        return (0 until 7).map { i ->
            val chord = when (level) {
                ChordLevel.TRIADS -> stackedChord(sn, i, 3)
                ChordLevel.SEVENTHS, ChordLevel.EXTENSIONS -> stackedChord(sn, i, 4)
            }
            val extensions = if (level == ChordLevel.EXTENSIONS) extensionsFor(sn, i) else emptyList()
            val index = i + 1
            var tonalFn: TonalFunction? = null
            var modalFn: ModalFunction? = null
            var ambiguous = false
            if (scale.lens == Lens.TONAL) {
                val entry = TonalFunctionTable.entryFor(scale, index)
                tonalFn = entry.function
                ambiguous = entry.isAmbiguous
            } else {
                modalFn = when {
                    index == 1 -> ModalFunction.CENTER
                    charNote != null && chord.notes.any { it.pitchClass == charNote.pitchClass } ->
                        ModalFunction.CHARACTERISTIC
                    else -> ModalFunction.NEUTRAL
                }
            }
            Degree(
                index = index,
                roman = romanFor(index, chord.quality),
                chord = chord,
                tonalFunction = tonalFn,
                modalFunction = modalFn,
                isAmbiguous = ambiguous,
                pedagogicalKey = "${scale.pedagogicalAlias.name.lowercase()}.$index",
                extensions = extensions,
            )
        }
    }

    /**
     * Buscador por sensación (§6.2.7). Mapeo [DECISIÓN]:
     * REST → tónica / centro modal; MOTION → subdominante / neutro modal;
     * TENSION → dominante (modal: acordes con el tritono de la madre);
     * COLOR → extensiones sobre grados estables + característicos modales.
     * Orden: no-ambiguos primero; en TENSION el V7 primero.
     */
    fun searchByNeed(need: Need, level: ChordLevel): List<Degree> {
        if (scale.category == ScaleCategory.PENTATONIC) {
            return parentKey()!!.searchByNeed(need, level)
        }
        val field = diatonicField(level)
        return if (scale.lens == Lens.TONAL) {
            tonalSearch(need, field)
        } else {
            modalSearch(need, field)
        }
    }

    // ------------------------------------------------------------------ interno

    private fun tonalSearch(need: Need, field: List<Degree>): List<Degree> = when (need) {
        Need.REST -> field.filter { it.tonalFunction == TonalFunction.TONIC }
            .sortedWith(compareBy({ it.isAmbiguous }, { it.index }))
        Need.MOTION -> field.filter { it.tonalFunction == TonalFunction.SUBDOMINANT }
            .sortedWith(compareBy({ it.isAmbiguous }, { it.index }))
        Need.TENSION -> field.filter { it.tonalFunction == TonalFunction.DOMINANT }
            .sortedWith(compareBy({ it.chord.quality != ChordQuality.DOM7 }, { it.isAmbiguous }, { it.index }))
        Need.COLOR -> {
            val stable = field.filter { it.tonalFunction == TonalFunction.TONIC }
                .sortedWith(compareBy({ it.isAmbiguous }, { it.index }))
            extensionDegrees(stable.map { it.index })
        }
    }

    private fun modalSearch(need: Need, field: List<Degree>): List<Degree> = when (need) {
        Need.REST -> field.filter { it.modalFunction == ModalFunction.CENTER }
        Need.MOTION -> field.filter { it.modalFunction == ModalFunction.NEUTRAL }.sortedBy { it.index }
        Need.TENSION -> {
            // Tensión en lente modal: acordes que contienen el tritono de la
            // mayor madre (grados 4 y 7 de la madre). La UI acompaña con nota
            // explicativa: esa tensión "tira" hacia el mayor relativo.
            val parentNotes = parentKey()!!.notes()
            val tritone = setOf(parentNotes[3].pitchClass, parentNotes[6].pitchClass)
            field.filter { d -> tritone.all { pc -> d.chord.notes.any { it.pitchClass == pc } } }
                .sortedWith(compareBy({ it.chord.quality != ChordQuality.DOM7 }, { it.index }))
        }
        Need.COLOR -> {
            val characteristic = field.filter { it.modalFunction == ModalFunction.CHARACTERISTIC }
                .sortedBy { it.index }
            characteristic + extensionDegrees(listOf(1))
        }
    }

    /** Grados-resultado con cada acorde de extensión de los grados [indices]. */
    private fun extensionDegrees(indices: List<Int>): List<Degree> {
        val extField = diatonicField(ChordLevel.EXTENSIONS)
        return indices.flatMap { idx ->
            val src = extField.first { it.index == idx }
            src.extensions.map { ext ->
                src.copy(chord = ext, roman = romanFor(src.index, ext.quality), extensions = emptyList())
            }
        }
    }

    private fun tonalNotes(): List<Note> = when (scale) {
        ScaleType.MAJOR -> byPattern(tonic, ScaleType.MAJOR.pattern)
        ScaleType.NATURAL_MINOR -> byPattern(tonic, ScaleType.NATURAL_MINOR.pattern)
        // Armónica/melódica: derivadas de la natural elevando grados con el
        // accidente correcto sobre la grafía de la natural (§3.2).
        ScaleType.HARMONIC_MINOR -> raise(byPattern(tonic, ScaleType.NATURAL_MINOR.pattern), 7)
        ScaleType.MELODIC_MINOR -> raise(raise(byPattern(tonic, ScaleType.NATURAL_MINOR.pattern), 6), 7)
        else -> throw IllegalStateException("No es escala tonal: $scale")
    }

    /** Los modos comparten las notas de su mayor madre: rotación, no tabla (§3.2). */
    private fun modeNotesByRotation(): List<Note> {
        val parentNotes = parentMajor().notes()
        val shift = scale.modeIndex!! - 1
        val rotated = List(7) { parentNotes[(it + shift).mod(7)] }
        check(rotated[0] == tonic) { "Rotación inconsistente para $this" }
        return rotated
    }

    private fun parentMajor(): Key {
        val m = scale.modeIndex!!
        val offset = ScaleType.MAJOR.pattern.take(m - 1).sum()
        val letter = Note.letterAt(tonic.letter, -(m - 1))
        val pc = (tonic.pitchClass - offset).mod(12)
        return Key(Note.spelled(letter, pc), ScaleType.MAJOR)
    }

    companion object {

        /** Escala de 7 notas por patrón: una letra por grado, alteración mínima. */
        internal fun byPattern(tonic: Note, pattern: List<Int>): List<Note> {
            require(pattern.size == 7) { "Patrón no heptatónico: $pattern" }
            val result = mutableListOf(tonic)
            var pc = tonic.pitchClass
            for (i in 1 until 7) {
                pc = (pc + pattern[i - 1]).mod(12)
                result += Note.spelled(Note.letterAt(tonic.letter, i), pc)
            }
            return result
        }

        private fun raise(notes: List<Note>, degree: Int): List<Note> =
            notes.mapIndexed { i, n -> if (i == degree - 1) Note(n.letter, n.accidental + 1) else n }

        /** Acorde apilando terceras de la escala: [size] = 3 (tríada) o 4 (cuatríada). */
        internal fun stackedChord(sn: List<Note>, rootIndex: Int, size: Int): Chord {
            val letterSteps = listOf(0, 2, 4, 6).take(size)
            val notes = letterSteps.map { sn[(rootIndex + it).mod(7)] }
            val offsets = stackOffsets(notes)
            val quality = qualityOf(offsets)
                ?: throw IllegalStateException("Acorde no estándar en ${sn[rootIndex]}: $offsets")
            return Chord(notes[0], quality, notes, offsets, caution = false)
        }

        /** Offsets apilados: cada nota por encima de la anterior (voicing cerrado). */
        private fun stackOffsets(notes: List<Note>): List<Int> {
            val offsets = mutableListOf(0)
            var acc = 0
            for (i in 1 until notes.size) {
                acc += (notes[i].pitchClass - notes[i - 1].pitchClass).mod(12)
                offsets += acc
            }
            return offsets
        }

        private fun qualityOf(offsets: List<Int>): ChordQuality? = when (offsets) {
            listOf(0, 4, 7) -> ChordQuality.MAJOR
            listOf(0, 3, 7) -> ChordQuality.MINOR
            listOf(0, 3, 6) -> ChordQuality.DIMINISHED
            listOf(0, 4, 8) -> ChordQuality.AUGMENTED
            listOf(0, 4, 7, 11) -> ChordQuality.MAJ7
            listOf(0, 4, 7, 10) -> ChordQuality.DOM7
            listOf(0, 3, 7, 10) -> ChordQuality.MIN7
            listOf(0, 3, 6, 10) -> ChordQuality.MIN7B5
            listOf(0, 3, 6, 9) -> ChordQuality.DIM7
            listOf(0, 3, 7, 11) -> ChordQuality.MINMAJ7
            listOf(0, 4, 8, 11) -> ChordQuality.MAJ7SHARP5
            else -> null
        }

        /**
         * Nivel extensiones (§3.5): vocabulario cerrado generado por regla,
         * exclusivamente con notas de la escala activa. La nomenclatura exige
         * la calidad correcta del intervalo definitorio (m9 = m7 + 9ª MAYOR);
         * el acorde `13` incluye la 11ª de la escala a propósito: cuando forma
         * 9ª menor con la 3ª mayor se etiqueta con precaución y la ficha
         * explica por qué en la práctica se omite (§3.5: no ocultarlo).
         */
        internal fun extensionsFor(sn: List<Note>, rootIndex: Int): List<Chord> {
            val root = sn[rootIndex]
            fun toneAt(steps: Int): Note = sn[(rootIndex + steps).mod(7)]
            fun interval(steps: Int, compound: Boolean = false): Int {
                val simple = (toneAt(steps).pitchClass - root.pitchClass).mod(12)
                return if (compound) simple + 12 else simple
            }

            val third = interval(2)
            val fifth = interval(4)
            val seventh = interval(6)
            val second = interval(1)
            val fourth = interval(3)
            val sixth = interval(5)
            val ninth = interval(1, compound = true)
            val eleventh = interval(3, compound = true)
            val thirteenth = interval(5, compound = true)

            val triadQ = qualityOf(listOf(0, third, fifth))
            val seventhQ = qualityOf(listOf(0, third, fifth, seventh))

            val result = mutableListOf<Chord>()
            fun add(quality: ChordQuality, offsets: List<Int>, letterSteps: List<Int>) {
                val notes = letterSteps.map { toneAt(it) }
                result += Chord(root, quality, notes, offsets, hasMinorNinthClash(offsets))
            }

            if (triadQ == ChordQuality.MAJOR && ninth == 14) {
                add(ChordQuality.ADD9, listOf(0, 4, 7, 14), listOf(0, 2, 4, 1))
            }
            if (second == 2 && fifth == 7) {
                add(ChordQuality.SUS2, listOf(0, 2, 7), listOf(0, 1, 4))
            }
            if (fourth == 5 && fifth == 7) {
                add(ChordQuality.SUS4, listOf(0, 5, 7), listOf(0, 3, 4))
            }
            if (triadQ == ChordQuality.MAJOR && sixth == 9) {
                add(ChordQuality.SIX, listOf(0, 4, 7, 9), listOf(0, 2, 4, 5))
            }
            if (triadQ == ChordQuality.MINOR && sixth == 9) {
                add(ChordQuality.MIN6, listOf(0, 3, 7, 9), listOf(0, 2, 4, 5))
            }
            if (triadQ == ChordQuality.MAJOR && sixth == 9 && ninth == 14) {
                add(ChordQuality.SIX_NINE, listOf(0, 4, 7, 9, 14), listOf(0, 2, 4, 5, 1))
            }
            if (seventhQ == ChordQuality.MAJ7 && ninth == 14) {
                add(ChordQuality.MAJ9, listOf(0, 4, 7, 11, 14), listOf(0, 2, 4, 6, 1))
            }
            if (seventhQ == ChordQuality.MIN7 && ninth == 14) {
                add(ChordQuality.MIN9, listOf(0, 3, 7, 10, 14), listOf(0, 2, 4, 6, 1))
            }
            if (seventhQ == ChordQuality.DOM7 && ninth == 14) {
                add(ChordQuality.NINE, listOf(0, 4, 7, 10, 14), listOf(0, 2, 4, 6, 1))
            }
            if (fourth == 5 && fifth == 7 && seventh == 10) {
                add(ChordQuality.DOM7SUS4, listOf(0, 5, 7, 10), listOf(0, 3, 4, 6))
            }
            if (seventhQ == ChordQuality.MIN7 && eleventh == 17) {
                // La 9ª solo se incluye si es mayor; si la escala da 9ª menor
                // se omite del voicing (el nombre m11 no la implica).
                if (ninth == 14) {
                    add(ChordQuality.MIN11, listOf(0, 3, 7, 10, 14, 17), listOf(0, 2, 4, 6, 1, 3))
                } else {
                    add(ChordQuality.MIN11, listOf(0, 3, 7, 10, 17), listOf(0, 2, 4, 6, 3))
                }
            }
            if (seventhQ == ChordQuality.DOM7 && ninth == 14 && thirteenth == 21) {
                add(
                    ChordQuality.THIRTEEN,
                    listOf(0, 4, 7, 10, 14, eleventh, 21),
                    listOf(0, 2, 4, 6, 1, 3, 5),
                )
            }
            return result
        }
    }
}
