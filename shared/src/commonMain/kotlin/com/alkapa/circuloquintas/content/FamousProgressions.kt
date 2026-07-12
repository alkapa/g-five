package com.alkapa.circuloquintas.content

import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.Note
import com.alkapa.circuloquintas.domain.Progression
import com.alkapa.circuloquintas.domain.ProgressionChord
import com.alkapa.circuloquintas.domain.ScaleType

/**
 * Progresión famosa precargada (§6.5): solo lectura, con ficha breve.
 * "Duplicar" en la biblioteca la convierte en progresión editable propia.
 */
data class FamousProgression(
    val name: String,
    val romanSummary: String,
    val progression: Progression,
    val suggestedLevel: ChordLevel,
    val ficha: String,
)

object FamousProgressions {

    private fun chord(degree: Int, quality: ChordQuality, beats: Int = 4) =
        ProgressionChord(degree, quality, beats)

    private fun progression(name: String, tonic: Note, scale: ScaleType, chords: List<ProgressionChord>) =
        Progression(
            id = null,
            name = name,
            key = Key(tonic, scale),
            bpm = 90,
            chords = chords,
            readOnly = true,
        )

    val all: List<FamousProgression> = listOf(
        FamousProgression(
            name = "La progresión pop",
            romanSummary = "I–V–vi–IV",
            progression = progression(
                "La progresión pop", Note('C', 0), ScaleType.MAJOR,
                listOf(
                    chord(1, ChordQuality.MAJOR),
                    chord(5, ChordQuality.MAJOR),
                    chord(6, ChordQuality.MINOR),
                    chord(4, ChordQuality.MAJOR),
                ),
            ),
            suggestedLevel = ChordLevel.TRIADS,
            ficha = "Encadena reposo (I), tensión (V), reposo relativo (vi) y paso (IV): " +
                "un ciclo completo de llegada y movimiento que nunca se aleja de casa. " +
                "Es una de las vueltas más usadas del pop de las últimas décadas. " +
                "Funciona en cualquier tonalidad mayor, a cualquier tempo.",
        ),
        FamousProgression(
            name = "Pop emotiva",
            romanSummary = "vi–IV–I–V",
            progression = progression(
                "Pop emotiva", Note('C', 0), ScaleType.MAJOR,
                listOf(
                    chord(6, ChordQuality.MINOR),
                    chord(4, ChordQuality.MAJOR),
                    chord(1, ChordQuality.MAJOR),
                    chord(5, ChordQuality.MAJOR),
                ),
            ),
            suggestedLevel = ChordLevel.TRIADS,
            ficha = "Usa los mismos acordes que I–V–vi–IV pero empezando por el vi: " +
                "al arrancar desde el reposo relativo menor, el color general se percibe " +
                "más melancólico aunque los acordes sean los mismos. Buen ejemplo de cómo " +
                "el punto de partida cambia la sensación de una progresión.",
        ),
        FamousProgression(
            name = "Años 50 / doo-wop",
            romanSummary = "I–vi–IV–V",
            progression = progression(
                "Años 50 / doo-wop", Note('C', 0), ScaleType.MAJOR,
                listOf(
                    chord(1, ChordQuality.MAJOR),
                    chord(6, ChordQuality.MINOR),
                    chord(4, ChordQuality.MAJOR),
                    chord(5, ChordQuality.MAJOR),
                ),
            ),
            suggestedLevel = ChordLevel.TRIADS,
            ficha = "Clásica del doo-wop y de las baladas de los años 50. El vi suaviza el " +
                "camino del I hacia el IV, y el V final pide volver a empezar, lo que la " +
                "hace ideal para repetir en bucle.",
        ),
        FamousProgression(
            name = "La cadencia del jazz",
            romanSummary = "ii–V–I",
            progression = progression(
                "La cadencia del jazz", Note('C', 0), ScaleType.MAJOR,
                listOf(
                    chord(2, ChordQuality.MIN7),
                    chord(5, ChordQuality.DOM7),
                    chord(1, ChordQuality.MAJ7, beats = 8),
                ),
            ),
            suggestedLevel = ChordLevel.SEVENTHS,
            ficha = "El ii prepara al V y el V resuelve en el I: es la cadencia más común " +
                "del jazz. Con séptimas (iim7–V7–Imaj7) cada acorde gana el color " +
                "característico del género; conviene escucharla en el nivel Séptimas.",
        ),
        FamousProgression(
            name = "Rock y blues básico",
            romanSummary = "I–IV–V",
            progression = progression(
                "Rock y blues básico", Note('C', 0), ScaleType.MAJOR,
                listOf(
                    chord(1, ChordQuality.MAJOR),
                    chord(4, ChordQuality.MAJOR),
                    chord(5, ChordQuality.MAJOR),
                ),
            ),
            suggestedLevel = ChordLevel.TRIADS,
            ficha = "Los tres pilares de la tonalidad: reposo (I), paso (IV) y tensión (V). " +
                "Con solo estos tres grados se acompaña una enorme cantidad de rock, blues " +
                "y música popular.",
        ),
        FamousProgression(
            name = "Menor épica",
            romanSummary = "i–VI–III–VII",
            progression = progression(
                "Menor épica", Note('A', 0), ScaleType.NATURAL_MINOR,
                listOf(
                    chord(1, ChordQuality.MINOR),
                    chord(6, ChordQuality.MAJOR),
                    chord(3, ChordQuality.MAJOR),
                    chord(7, ChordQuality.MAJOR),
                ),
            ),
            suggestedLevel = ChordLevel.TRIADS,
            ficha = "Recorre los grados mayores del modo menor (VI, III, VII), que aportan " +
                "brillo sin salir de la escala. Muy usada en rock épico y baladas; el VII " +
                "final empuja de vuelta al i con un sonido modal característico.",
        ),
        FamousProgression(
            name = "Cadencia andaluza",
            romanSummary = "i–VII–VI–V",
            progression = progression(
                "Cadencia andaluza", Note('A', 0), ScaleType.NATURAL_MINOR,
                listOf(
                    chord(1, ChordQuality.MINOR),
                    chord(7, ChordQuality.MAJOR),
                    chord(6, ChordQuality.MAJOR),
                    // Préstamo armónico deliberado: V MAYOR (de la menor armónica)
                    // en lugar del v menor diatónico. La ficha lo explica (§6.5).
                    chord(5, ChordQuality.MAJOR),
                ),
            ),
            suggestedLevel = ChordLevel.TRIADS,
            ficha = "Descenso i–VII–VI–V típico del flamenco y de mucha música española. " +
                "Atención: el V final se toca MAYOR, un préstamo de la menor armónica que " +
                "aporta la sensible y una tensión de dominante real. Para verlo en el " +
                "círculo, cambia la escala a menor armónica: ahí el V mayor es diatónico.",
        ),
        FamousProgression(
            name = "Vamp dórico",
            romanSummary = "i–IV",
            progression = progression(
                "Vamp dórico", Note('D', 0), ScaleType.DORIAN,
                listOf(
                    chord(1, ChordQuality.MINOR),
                    chord(4, ChordQuality.MAJOR),
                ),
            ),
            suggestedLevel = ChordLevel.TRIADS,
            ficha = "Dos acordes que definen el modo: el i menor como casa y el IV MAYOR, " +
                "posible gracias a la 6ª mayor dórica — es LA marca del dórico. Sostener " +
                "este vaivén es la manera más directa de que suene dórico y no menor natural.",
        ),
        FamousProgression(
            name = "Vamp mixolidio",
            romanSummary = "I–VII",
            progression = progression(
                "Vamp mixolidio", Note('G', 0), ScaleType.MIXOLYDIAN,
                listOf(
                    chord(1, ChordQuality.MAJOR),
                    chord(7, ChordQuality.MAJOR),
                ),
            ),
            suggestedLevel = ChordLevel.TRIADS,
            ficha = "El I mayor alterna con el acorde mayor construido sobre la 7ª menor " +
                "del modo. Los guitarristas suelen llamarlo \"bVII\" (comparándolo con la " +
                "escala mayor); aquí lo numeramos VII porque la numeración siempre es " +
                "relativa a la escala activa. Sonido clásico de rock y jam.",
        ),
    )
}
