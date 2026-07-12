package com.alkapa.circuloquintas.content

import com.alkapa.circuloquintas.domain.Degree
import com.alkapa.circuloquintas.domain.ScaleType

/** Ficha pedagógica de un grado (§6.2.8): explicación breve + uso típico. */
data class DegreeFicha(
    val explanation: String,
    val typicalUse: String,
)

/** Ficha de escala/modo: carácter, y consejo práctico (nota característica, etc.). */
data class ScaleFicha(
    val name: String,
    val description: String,
    val tip: String,
)

/**
 * Punto de acceso al contenido pedagógico estático (§8: datos Kotlin en
 * commonMain). Los textos viven en [PedagogicalData] (generados y revisados;
 * sujetos a validación humana experta antes del release, según PRD).
 */
object PedagogicalContent {

    /** Ficha por clave pedagógica ("major.5", "dorian.4"...). */
    fun degreeFicha(pedagogicalKey: String): DegreeFicha? =
        PedagogicalData.degreeFichas[pedagogicalKey]

    fun degreeFicha(degree: Degree): DegreeFicha? = degreeFicha(degree.pedagogicalKey)

    fun scaleFicha(scale: ScaleType): ScaleFicha =
        PedagogicalData.scaleFichas.getValue(scale)

    fun scaleName(scale: ScaleType): String = scaleFicha(scale).name

    /** Introducción del modelo funcional: mapa pedagógico, no verdad absoluta (§4). */
    val functionModelIntro: String get() = PedagogicalData.functionModelIntro

    /** Explicación de la etiqueta "precaución" (choque de 9ª menor, §3.5). */
    val cautionExplanation: String get() = PedagogicalData.cautionExplanation

    /** Nota del buscador de Tensión en lente modal (§6.2.7). */
    val modalTensionNote: String get() = PedagogicalData.modalTensionNote

    /** Etiquetas cortas de función para la UI (formal + coloquial, §4/§5). */
    fun tonalFunctionLabel(fn: com.alkapa.circuloquintas.domain.TonalFunction?): String = when (fn) {
        com.alkapa.circuloquintas.domain.TonalFunction.TONIC -> "Tónica · Descanso"
        com.alkapa.circuloquintas.domain.TonalFunction.SUBDOMINANT -> "Subdominante · De paso"
        com.alkapa.circuloquintas.domain.TonalFunction.DOMINANT -> "Dominante · Tensión"
        null -> "Color · Inestable"
    }

    fun modalFunctionLabel(fn: com.alkapa.circuloquintas.domain.ModalFunction): String = when (fn) {
        com.alkapa.circuloquintas.domain.ModalFunction.CENTER -> "Centro modal · Casa"
        com.alkapa.circuloquintas.domain.ModalFunction.CHARACTERISTIC -> "Característico · Color del modo"
        com.alkapa.circuloquintas.domain.ModalFunction.NEUTRAL -> "Neutro"
    }
}
