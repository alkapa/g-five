package com.alkapa.circuloquintas.content

import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.key
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PedagogicalContentTest {

    private val referenceTonics = mapOf(
        ScaleType.MAJOR to "C", ScaleType.NATURAL_MINOR to "A",
        ScaleType.HARMONIC_MINOR to "A", ScaleType.MELODIC_MINOR to "A",
        ScaleType.IONIAN to "C", ScaleType.DORIAN to "D", ScaleType.PHRYGIAN to "E",
        ScaleType.LYDIAN to "F", ScaleType.MIXOLYDIAN to "G", ScaleType.AEOLIAN to "A",
        ScaleType.LOCRIAN to "B", ScaleType.PENT_MAJOR to "C", ScaleType.PENT_MINOR to "A",
    )

    @Test
    fun existeFichaNoVaciaParaCadaClavePedagogica() {
        // Recorre TODOS los pedagogicalKey que el motor puede producir
        // (13 escalas × 7 grados, con alias jónico→mayor y eólico→menor).
        for ((scale, tonic) in referenceTonics) {
            for (degree in key(tonic, scale).diatonicField(ChordLevel.SEVENTHS)) {
                val ficha = PedagogicalContent.degreeFicha(degree)
                assertNotNull(ficha, "Falta ficha para ${degree.pedagogicalKey}")
                assertTrue(ficha.explanation.isNotBlank(), "Explicación vacía: ${degree.pedagogicalKey}")
                assertTrue(ficha.typicalUse.isNotBlank(), "Uso típico vacío: ${degree.pedagogicalKey}")
            }
        }
    }

    @Test
    fun hayFichaDeEscalaParaLasTrece() {
        for (scale in ScaleType.entries) {
            val ficha = PedagogicalContent.scaleFicha(scale)
            assertTrue(ficha.name.isNotBlank(), "Nombre vacío: $scale")
            assertTrue(ficha.description.isNotBlank(), "Descripción vacía: $scale")
            assertTrue(ficha.tip.isNotBlank(), "Tip vacío: $scale")
        }
    }

    @Test
    fun textosDelModeloFuncional() {
        // La intro DEBE declarar el modelo como mapa pedagógico, no verdad absoluta (§4).
        assertTrue(PedagogicalContent.functionModelIntro.contains("mapa", ignoreCase = true))
        assertTrue(PedagogicalContent.cautionExplanation.isNotBlank())
        assertTrue(PedagogicalContent.modalTensionNote.isNotBlank())
    }

    @Test
    fun mencionesObligatorias() {
        fun ficha(k: String) = PedagogicalContent.degreeFicha(k)!!.let { it.explanation + " " + it.typicalUse }
        // v de menor natural: préstamo del V mayor de la armónica (§4.2).
        assertTrue(ficha("natural_minor.5").contains("armónica", ignoreCase = true))
        // VII mixolidio: apodo guitarrero bVII (§3.3).
        assertTrue(ficha("mixolydian.7").contains("bVII"))
        // Todo grado ambiguo menciona su ambigüedad (§4, restricción).
        val ambiguous = mapOf(
            "major" to listOf(3, 6, 7),
            "natural_minor" to listOf(3, 5, 6, 7),
            "harmonic_minor" to listOf(3, 6),
            "melodic_minor" to listOf(3, 4, 6),
        )
        for ((scale, degrees) in ambiguous) {
            for (d in degrees) {
                assertTrue(
                    ficha("$scale.$d").contains("ambig", ignoreCase = true),
                    "$scale.$d no menciona la ambigüedad",
                )
            }
        }
    }
}
