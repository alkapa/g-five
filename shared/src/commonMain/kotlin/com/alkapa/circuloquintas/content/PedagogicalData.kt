package com.alkapa.circuloquintas.content

import com.alkapa.circuloquintas.domain.ScaleType

/**
 * Contenido pedagógico estático (generado y revisado en dos pasadas: rigor
 * teórico + menciones obligatorias/estilo). Español neutro. Sujeto a
 * validación por una persona con formación musical formal antes del release
 * (criterio del PRD, §1.3).
 */
internal object PedagogicalData {

    val degreeFichas: Map<String, DegreeFicha> = mapOf(
        "major.1" to DegreeFicha(
            explanation = "El primer grado es el acorde de Tónica, la función de Descanso. En C mayor es el acorde C, y funciona como el centro absoluto de reposo de la tonalidad: todos los demás acordes generan movimiento que tarde o temprano quiere regresar aquí. Cuando una progresión llega a este acorde, la música se asienta y sientes esa calma de haber llegado a casa.",
            typicalUse = "Suele abrir y cerrar las progresiones, marcando el punto de partida y de llegada. Lo escuchas en progresiones como I-IV-V-I (C-F-G-C) o I-vi-IV-V (C-Am-F-G).",
        ),
        "major.2" to DegreeFicha(
            explanation = "El segundo grado es un acorde menor con función de Subdominante, también llamada función De paso. En C mayor es Dm. Es un acorde de movimiento: aleja la música del reposo y prepara la llegada de la Dominante, creando la sensación de que algo está a punto de resolverse.",
            typicalUse = "Su uso más típico es preparar al quinto grado, como en la progresión ii-V-I (Dm-G-C), una de las fórmulas más comunes de la música tonal.",
        ),
        "major.3" to DegreeFicha(
            explanation = "El tercer grado es un acorde menor que suele clasificarse dentro de la función de Tónica (Descanso), pero es el más ambiguo del campo armónico. En C mayor es Em, y comparte dos notas con C (E y G) y dos notas con G (G y B), así que puede sonar como un reposo débil o como un puente hacia la tensión, según el contexto. Recuerda que el modelo de tres funciones es un mapa pedagógico para orientarte, no una regla absoluta, y este acorde es el mejor ejemplo de ello.",
            typicalUse = "Funciona bien como acorde de enlace o de color, por ejemplo en I-iii-vi-IV (C-Em-Am-F) o en I-iii-IV-V (C-Em-F-G).",
        ),
        "major.4" to DegreeFicha(
            explanation = "El cuarto grado es la Subdominante por excelencia, el acorde De paso clásico del campo armónico. En C mayor es F. Al sonar, la música se aleja suavemente del reposo y empieza a caminar: todavía no hay una tensión fuerte, pero sí una clara sensación de movimiento.",
            typicalUse = "Aparece constantemente entre la tónica y la dominante, como en I-IV-V (C-F-G), y también puede regresar directo al reposo, como en I-IV-I (C-F-C).",
        ),
        "major.5" to DegreeFicha(
            explanation = "El quinto grado es el acorde de Dominante, la función de Tensión. En C mayor es G, y contiene la sensible de la tonalidad, la nota B, que está a solo medio tono de C y empuja con fuerza hacia ella. Es el punto de máxima tensión del campo armónico: cuando suena, el oído pide volver a la tónica. En su versión de cuatro notas, G7, esa tensión se vuelve aún más evidente.",
            typicalUse = "Su papel típico es cerrar frases resolviendo en la tónica, como en V-I (G-C), o dentro de progresiones como I-IV-V-I y ii-V-I.",
        ),
        "major.6" to DegreeFicha(
            explanation = "El sexto grado es un acorde menor con función de Tónica (Descanso), pero de una forma ambigua: ofrece un reposo relativo, más suave y melancólico que el del primer grado. En C mayor es Am, el relativo menor de C, con el que comparte dos notas (C y E), y por eso puede sustituirlo sin romper la sensación de calma. No descansa con la misma firmeza que I, aunque tampoco genera tensión.",
            typicalUse = "Se usa muchísimo como sustituto suave de la tónica, por ejemplo en I-vi-IV-V (C-Am-F-G) o en la muy popular vi-IV-I-V (Am-F-C-G).",
        ),
        "major.7" to DegreeFicha(
            explanation = "El séptimo grado es un acorde disminuido con función de Dominante (Tensión), aunque su papel es ambiguo: más que un acorde independiente, suele comportarse como un V al que le falta la fundamental. En C mayor es B°, y comparte con G7 el tritono entre las notas B y F, el intervalo que genera esa urgencia de resolver. Es el acorde más inestable del campo armónico y casi nunca funciona como punto de llegada.",
            typicalUse = "Se emplea sobre todo para empujar hacia la tónica, como en vii°-I (B°-C), o como paso de tensión en progresiones tipo IV-vii°-I (F-B°-C).",
        ),
        "natural_minor.1" to DegreeFicha(
            explanation = "El grado i es el centro de gravedad de la tonalidad menor: en A menor natural corresponde al acorde Am. Su función es Tónica, también llamada Descanso, porque es el lugar donde la música se siente en casa y donde las frases pueden terminar con sensación de cierre. Todos los demás acordes del campo armónico se escuchan en relación con él.",
            typicalUse = "Suele abrir y cerrar las progresiones en menor, como en i-iv-v-i (Am-Dm-Em-Am) o i-VI-VII-i (Am-F-G-Am).",
        ),
        "natural_minor.2" to DegreeFicha(
            explanation = "El grado ii° es un acorde disminuido: en A menor natural es B°. Funciona como Subdominante, es decir, un acorde De paso que aleja la música del reposo y prepara la llegada de la dominante, un movimiento muy típico de la tonalidad menor. Por su sonido inestable casi nunca se usa como punto de llegada, sino como puente hacia otro acorde.",
            typicalUse = "Aparece sobre todo preparando la dominante, como en i-ii°-v-i o ii°-v-i (B°-Em-Am).",
        ),
        "natural_minor.3" to DegreeFicha(
            explanation = "El grado III es un acorde mayor: en A menor natural es C. Se clasifica como Tónica (Descanso), pero es un grado ambiguo: C es el relativo mayor de A menor, así que funciona como un reposo alternativo con un color más brillante que el de i. Recuerda que el modelo de tres funciones es un mapa pedagógico, no una regla absoluta: según el contexto, este acorde puede sentirse como descanso o simplemente como un cambio de color.",
            typicalUse = "Es muy común en progresiones como i-VI-III-VII (Am-F-C-G), donde aporta un momento de brillo dentro del modo menor.",
        ),
        "natural_minor.4" to DegreeFicha(
            explanation = "El grado iv es un acorde menor: en A menor natural es Dm. Cumple la función de Subdominante, o acorde De paso: saca la música del reposo y la pone en movimiento, normalmente en dirección a la dominante. Su color menor le da un sonido suave y melancólico, muy característico de este modo.",
            typicalUse = "Sirve de puente entre el reposo y la tensión, como en i-iv-v-i (Am-Dm-Em-Am) o i-iv-VII (Am-Dm-G).",
        ),
        "natural_minor.5" to DegreeFicha(
            explanation = "El grado v es un acorde menor: en A menor natural es Em. Su función es de Dominante (Tensión), pero es un grado ambiguo: su tensión es suave, porque al ser un acorde menor no contiene la sensible, la nota que empuja con fuerza hacia la tónica. Por eso, mucha música en tonalidad menor toma el V mayor (en este caso E) de la escala menor armónica cuando busca una tensión real hacia i. En la app basta con cambiar la escala a menor armónica para ver ese V mayor en el campo armónico.",
            typicalUse = "Funciona en cadencias suaves como i-iv-v-i (Am-Dm-Em-Am), con un sonido menos tenso que el de una dominante mayor.",
        ),
        "natural_minor.6" to DegreeFicha(
            explanation = "El grado VI es un acorde mayor: en A menor natural es F. Se clasifica como Subdominante (De paso), porque suele poner la música en movimiento, pero es un grado ambiguo: comparte dos notas con el acorde de tónica y muchas veces también suena como un reposo alternativo. Deja que tu oído decida qué papel cumple en cada progresión: las tres funciones son una guía, no una etiqueta fija.",
            typicalUse = "Es protagonista de progresiones muy comunes en menor, como i-VI-III-VII (Am-F-C-G) o VI-VII-i (F-G-Am).",
        ),
        "natural_minor.7" to DegreeFicha(
            explanation = "El grado VII es un acorde mayor: en A menor natural es G. Se marca como Dominante (Tensión), pero es un caso ambiguo: al no contener la sensible, su tensión es suave y no empuja hacia la tónica con la fuerza de una dominante clásica. Su sonido abierto es muy típico del rock y de la música de aire modal, donde el paso de VII a i resulta natural aunque menos tenso.",
            typicalUse = "Brilla en progresiones de rock y pop en menor, como i-VII-VI (Am-G-F) o i-VI-VII-i (Am-F-G-Am).",
        ),
        "harmonic_minor.1" to DegreeFicha(
            explanation = "Es el acorde de Tónica, tu punto de partida y de llegada: en A menor armónica es Am. Funciona como el hogar de la tonalidad y produce sensación de Descanso, de que la música ya llegó a su lugar. Cuando una progresión vuelve a este acorde, el oído siente que la frase se cierra.",
            typicalUse = "Se usa para abrir y cerrar progresiones, marcando el centro de la tonalidad. Ejemplos: i-iv-V-i o i-VI-iv-V, donde todo el movimiento termina regresando al i.",
        ),
        "harmonic_minor.2" to DegreeFicha(
            explanation = "Es una triada disminuida construida sobre el segundo grado: en A menor armónica es B°. Cumple función de Subdominante, es decir, un acorde De paso que te aleja del descanso y empieza a generar movimiento. Por ser disminuido suena inestable y algo tenso, lo que lo hace perfecto para preparar la llegada del acorde de Dominante.",
            typicalUse = "Su uso más típico es preparar el V antes de resolver a la tónica, como en ii°-V-i. También aparece en progresiones como i-ii°-V-i para dar sensación de avance.",
        ),
        "harmonic_minor.3" to DegreeFicha(
            explanation = "Es una triada aumentada: en A menor armónica es C+. Este acorde no encaja con claridad en el mapa de las tres funciones (Descanso, De paso, Tensión), y esa ambigüedad es normal: el modelo de funciones es una guía pedagógica, no una regla absoluta. Su sonido es flotante e inestable, así que se suele tratar como un acorde de color, usado de forma puntual y breve más que como pilar de la progresión.",
            typicalUse = "Funciona bien como color de paso entre acordes más estables, por ejemplo en i-III+-iv o i-III+-VI. Conviene usarlo poco tiempo y con intención, como un condimento y no como plato principal.",
        ),
        "harmonic_minor.4" to DegreeFicha(
            explanation = "Es el acorde menor del cuarto grado: en A menor armónica es Dm. Cumple función de Subdominante, el clásico acorde De paso que saca a la música del descanso y la pone en camino. Suena estable pero con ganas de moverse, y suele actuar como puente natural hacia el acorde de Dominante.",
            typicalUse = "Aparece constantemente preparando la tensión del V, como en i-iv-V-i o iv-V-i. También puede alternar con la tónica para dar movimiento suave, como en i-iv-i.",
        ),
        "harmonic_minor.5" to DegreeFicha(
            explanation = "Es el acorde de Dominante y la verdadera razón de ser de esta escala: en A menor armónica es E, un acorde mayor. Al subir el séptimo grado de la escala aparece la sensible (G# en A menor), una nota que queda a solo medio tono de la tónica y que pide resolver hacia ella. Por eso este V mayor genera una Tensión fuerte y empuja con muchísima claridad hacia el i, algo que el V menor de la escala menor natural no logra con la misma fuerza.",
            typicalUse = "Su uso central es la cadencia V-i, el movimiento de tensión a descanso más potente de la tonalidad. Lo encuentras en progresiones como i-iv-V-i o ii°-V-i.",
        ),
        "harmonic_minor.6" to DegreeFicha(
            explanation = "Es el acorde mayor del sexto grado: en A menor armónica es F. En el mapa de funciones se ubica como Subdominante, un acorde De paso, pero es un grado ambiguo: comparte dos notas con el acorde de tónica, así que según el contexto también puede sonar bastante reposado, casi como un descanso alternativo. Esa doble cara es un buen recordatorio de que las tres funciones son una guía para orientarte, no una verdad absoluta.",
            typicalUse = "Suele usarse como paso intermedio que renueva el movimiento antes de volver a la tensión y al descanso, como en i-VI-iv-V o i-VI-ii°-V. También puede aparecer después del V para sorprender al oído en lugar de resolver directo al i.",
        ),
        "harmonic_minor.7" to DegreeFicha(
            explanation = "Es una triada disminuida construida directamente sobre la sensible: en A menor armónica es G#°. Cumple función de Dominante, es decir, pura Tensión: su nota fundamental está a medio tono de la tónica y todo el acorde suena inestable, con una necesidad clara de resolver al i. Por eso se siente como un pariente cercano del V, generando un empuje similar hacia el descanso.",
            typicalUse = "Su uso más común es resolver directamente a la tónica, como en vii°-i o i-iv-vii°-i. También funciona como acorde de paso muy breve justo antes del i, intensificando la llegada.",
        ),
        "melodic_minor.1" to DegreeFicha(
            explanation = "Es el acorde de tónica, el centro de gravedad de la tonalidad y su punto de descanso. En A menor melódica corresponde a Am: aunque esta escala eleva la 6a y la 7a al subir, la tríada del primer grado sigue siendo menor. Cuando la progresión llega aquí, se siente que la música vuelve a casa.",
            typicalUse = "Suele abrir y cerrar las progresiones, actuando como punto de partida y de llegada: por ejemplo i-IV-V-i (Am-D-E-Am) o ii-V-i (Bm-E-Am).",
        ),
        "melodic_minor.2" to DegreeFicha(
            explanation = "Es el segundo grado y cumple función de subdominante, es decir, de paso: aleja la música del descanso y la encamina hacia la tensión. Aquí hay una diferencia importante con la menor natural: gracias a la 6a elevada de la escala (F#), este acorde resulta menor (Bm) y no disminuido, por lo que suena más estable y cómodo de usar.",
            typicalUse = "Su papel principal es preparar al dominante: la progresión ii-V-i (Bm-E-Am) es de las más típicas de este campo armónico; también funciona en i-ii-V-i.",
        ),
        "melodic_minor.3" to DegreeFicha(
            explanation = "Sobre el tercer grado aparece una tríada aumentada (C+), un acorde de sonido flotante y algo misterioso. Es un grado ambiguo: no encaja con claridad en ninguna de las tres funciones clásicas (descanso, de paso o tensión), y ahí el mapa de funciones muestra sus límites, porque es una guía pedagógica y no una regla absoluta. Más que un pilar de la progresión, es un color puntual que llama la atención por su inestabilidad.",
            typicalUse = "Se usa de forma breve, como adorno o acorde de paso entre grados más estables, por ejemplo i-III+-IV (Am-C+-D); rara vez se sostiene mucho tiempo.",
        ),
        "melodic_minor.4" to DegreeFicha(
            explanation = "Es el cuarto grado y en principio hace de subdominante, el clásico acorde de paso que da movimiento sin generar tanta tensión como el dominante. Sin embargo, es un grado ambiguo: la 6a elevada de la escala (F#) lo vuelve mayor, y al agregarle la séptima se convierte en un acorde de 7a (D7), justo la sonoridad que asociamos con los acordes de tensión. Podría decirse que trabaja como subdominante, pero viste un color de dominante.",
            typicalUse = "Encaja bien como paso hacia la tensión, por ejemplo i-IV-V (Am-D-E), o alternando con la tónica para dar aire a la progresión (i-IV-i).",
        ),
        "melodic_minor.5" to DegreeFicha(
            explanation = "Es el dominante auténtico de la tonalidad: la 7a elevada de la escala (G#, la sensible) hace que este acorde sea mayor (E) y cree una tensión clara que pide resolver en la tónica. En esto la menor melódica coincide con la menor armónica: ambas ofrecen un V mayor con sensible, algo que la menor natural no tiene. Es el acorde que más empuja la música de vuelta al descanso.",
            typicalUse = "Es el motor de las cadencias: V-i (E-Am) y ii-V-i (Bm-E-Am) son sus movimientos más habituales.",
        ),
        "melodic_minor.6" to DegreeFicha(
            explanation = "Sobre la 6a elevada de la escala se forma una tríada disminuida (F#°), un acorde inestable que no se deja clasificar bien en el modelo de tres funciones. Funciona como un puente: un peldaño de paso que conecta zonas de la progresión sin ser él mismo un punto de apoyo. Esta ambigüedad es normal; el esquema de descanso, de paso y tensión es un mapa pedagógico, y este grado es de los que se salen de él.",
            typicalUse = "Se usa de paso y por poco tiempo, conectando acordes más estables: por ejemplo IV-vi°-V (D-F#°-E) camino a la cadencia.",
        ),
        "melodic_minor.7" to DegreeFicha(
            explanation = "Es la tríada disminuida construida sobre la sensible (G#°) y cumple función de dominante, es decir, de tensión: sus notas empujan con fuerza hacia la tónica. Comparte dos de sus notas con el acorde de V (E), así que puedes pensarlo como su pariente cercano, con la misma urgencia por resolver. Su sonido es más frágil e inestable que el de E, pero apunta al mismo destino.",
            typicalUse = "Se emplea justo antes del descanso: vii°-i (G#°-Am) funciona como alternativa al clásico V-i, por ejemplo en i-IV-vii°-i.",
        ),
        "dorian.1" to DegreeFicha(
            explanation = "Dm es el centro modal, la casa del modo dórico: el acorde donde la música descansa y al que todo quiere regresar, parecido a la tónica (el descanso) de una tonalidad. Como D dórico usa las mismas notas que C mayor, lo que hace que suene dórico y no C mayor es justamente sostener Dm como centro, volviendo a él una y otra vez. Produce una sensación de reposo estable pero con un aire menor suave, menos oscuro que otros modos menores.",
            typicalUse = "Suele abrir y cerrar las progresiones, muchas veces repetido como vamp largo para asentar el modo. Ejemplos: i-IV (Dm-G) o i-VII-i (Dm-C-Dm).",
        ),
        "dorian.2" to DegreeFicha(
            explanation = "Em es el acorde del segundo grado y uno de los colores del modo: contiene la nota B, la 6ª mayor de D dórico, que es justo la nota que diferencia al dórico del menor natural. Al pasar por Em, ese color característico se vuelve audible sin salir del ambiente menor. Aporta un movimiento suave y flotante, como de ida y vuelta alrededor de la casa.",
            typicalUse = "Funciona muy bien alternando con el centro modal o como acorde de paso hacia otros grados. Ejemplos: i-ii (Dm-Em) como vamp, o i-ii-IV (Dm-Em-G).",
        ),
        "dorian.3" to DegreeFicha(
            explanation = "F es el acorde mayor del tercer grado y un grado neutro: no contiene la nota característica del modo, así que por sí solo no define el sonido dórico, pero tampoco lo contradice. Aporta un color mayor y luminoso dentro del ambiente menor, útil para dar variedad a las progresiones.",
            typicalUse = "Se usa como acorde intermedio para enriquecer el camino de regreso a la casa. Ejemplos: i-III-IV (Dm-F-G) o i-VII-III (Dm-C-F).",
        ),
        "dorian.4" to DegreeFicha(
            explanation = "G es el cuarto grado y la marca sonora del dórico: en un ambiente menor esperarías un iv menor, pero aquí sale mayor porque su tercera es B, la 6ª mayor del modo. Ese contraste entre un centro menor (Dm) y un IV mayor produce una mezcla muy reconocible de melancolía y brillo. Si quieres que algo suene dórico de inmediato, este es el acorde que lo delata.",
            typicalUse = "El vamp i-IV (Dm-G) repetido en bucle es el sonido dórico por excelencia. También aparece en progresiones como i-ii-IV (Dm-Em-G) o i-IV-i-VII.",
        ),
        "dorian.5" to DegreeFicha(
            explanation = "Am es el quinto grado y aquí aparece menor, a diferencia del V mayor de la tonalidad mayor. Es un grado neutro: acompaña bien el ambiente del modo y permite alejarse de la casa sin generar la tensión fuerte de un acorde de dominante (la tensión que pide resolver ya). Por eso el regreso a Dm se siente suave y relajado, muy propio del sonido modal.",
            typicalUse = "Sirve para crear movimiento tranquilo antes de volver al centro modal. Ejemplos: i-v (Dm-Am) o i-IV-v (Dm-G-Am).",
        ),
        "dorian.6" to DegreeFicha(
            explanation = "B° es el sexto grado y otro color del modo, porque está construido sobre B, la 6ª mayor que define al dórico. Contiene el tritono F-B de la escala madre (C mayor), un intervalo muy inestable que pide resolución; por eso, si lo usas en exceso, la música tiende a irse hacia C, el mayor relativo, en lugar de quedarse en Dm. Suena tenso e inquieto: conviene dosificarlo y usarlo como una pincelada breve.",
            typicalUse = "Aparece sobre todo como acorde de paso corto que enseguida vuelve a la casa. Ejemplos: i-vi°-i (Dm-B°-Dm) o i-IV-vi°-i (Dm-G-B°-Dm).",
        ),
        "dorian.7" to DegreeFicha(
            explanation = "C es el séptimo grado, un acorde mayor que aporta apertura y frescura dentro del ambiente menor. Es un grado neutro, pero con un detalle importante: es el acorde del mayor relativo, ya que D dórico usa las notas de C mayor. Convive de forma natural con el modo, aunque si le das demasiado peso puede robarle protagonismo a Dm y hacer que todo empiece a sonar a C mayor; trátalo como visita, no como destino.",
            typicalUse = "Se usa mucho como puente que refresca la progresión antes de regresar a la casa. Ejemplos: i-VII-i (Dm-C-Dm) o i-VII-IV-i (Dm-C-G-Dm).",
        ),
        "phrygian.1" to DegreeFicha(
            explanation = "Em es el acorde que se construye sobre la primera nota de E frigio y funciona como el centro modal, la casa. Cumple el papel de la tónica, el punto de descanso: sobre él la música se siente estable y en reposo. Como el modo comparte sus notas con otras escalas, conviene volver seguido a Em para que el oído no pierda de vista cuál es el centro.",
            typicalUse = "Suele abrir y cerrar las progresiones para dejar claro el centro, como en i-II-i o i-vii-i. También funciona como acorde base o pedal sobre el que se mueven los demás grados.",
        ),
        "phrygian.2" to DegreeFicha(
            explanation = "F es el acorde mayor que vive a solo medio tono por encima de la casa, y esa cercanía es la marca del modo frigio. Lo hace característico la nota F, la segunda menor del modo, que al sonar tan pegada a Em genera una tensión oscura y exótica muy reconocible. Muchos guitarristas lo apodan bII, porque al compararlo con la escala mayor su fundamental queda un semitono más abajo de lo esperado; aquí lo numeramos II porque los grados se cuentan sobre la escala que estamos usando. Es el acorde que más rápido le dice al oído que estás en frigio, un color muy asociado al flamenco y al metal.",
            typicalUse = "El vaivén i-II (Em-F) define por sí solo el sonido frigio y puede sostener secciones enteras. También aparece en la bajada iv-III-II-i (Am-G-F-Em), aunque conviene matizar: el flamenco prototípico usa la cadencia andaluza, que llega a un acorde mayor (Am-G-F-E, con el V mayor prestado de la menor armónica); la versión que termina en Em es la variante puramente modal del frigio.",
        ),
        "phrygian.3" to DegreeFicha(
            explanation = "G es el acorde mayor del tercer grado y se considera neutro: pertenece a la escala pero no contiene la nota característica del modo, así que aporta variedad sin cambiar el carácter frigio. Suena luminoso y estable, y comparte dos notas con Em, por lo que el paso entre ambos resulta muy suave.",
            typicalUse = "Funciona bien como acorde de paso o de contraste, por ejemplo en i-III-II-i o dentro de la bajada iv-III-II-i.",
        ),
        "phrygian.4" to DegreeFicha(
            explanation = "Am es el acorde menor del cuarto grado y se comporta como un grado neutro: sus notas no incluyen la característica del modo, así que ni refuerza ni debilita el color frigio. Aporta un movimiento suave y melancólico que encaja de forma natural con Em, con el que comparte la nota E.",
            typicalUse = "Sirve para alargar progresiones sin alejarse de casa, como en i-iv-i, o como punto de partida de la bajada iv-III-II-i.",
        ),
        "phrygian.5" to DegreeFicha(
            explanation = "B° es una tríada disminuida (B, D, F) y es el acorde más inestable del campo: pura tensión. Es característico porque contiene F, la segunda menor del modo, pero además guarda el tritono F-B de la escala madre, una tensión que no resuelve hacia Em sino que tira con fuerza hacia C mayor. Por eso conviene dosificarlo: si abusas de él, el oído puede empezar a escuchar C como centro y perderse la sensación frigia.",
            typicalUse = "Úsalo con moderación y como acorde de paso breve, por ejemplo en i-v°-VI, regresando pronto a Em para reafirmar la casa.",
        ),
        "phrygian.6" to DegreeFicha(
            explanation = "C es el acorde mayor del sexto grado y funciona como grado neutro: no contiene la nota característica del modo, así que suma calidez y color sin alterar el carácter frigio. Comparte dos notas con Em (E y G), por eso la transición entre ambos suena muy natural.",
            typicalUse = "Aporta aire y contraste dentro de las progresiones, por ejemplo en i-VI-II-i o i-VI-iv-i, siempre volviendo a Em para no perder el centro.",
        ),
        "phrygian.7" to DegreeFicha(
            explanation = "Dm es el acorde menor del séptimo grado, un tono por debajo de la casa, y es característico porque contiene F, la segunda menor que define al modo frigio. Refuerza el color oscuro del modo de una forma más suave que el acorde de F mayor, y su cercanía con Em crea un movimiento ascendente muy natural de regreso a la casa.",
            typicalUse = "Se usa mucho como paso previo al regreso a casa, como en vii-i o en el vaivén i-vii-i.",
        ),
        "lydian.1" to DegreeFicha(
            explanation = "Es el acorde construido sobre F, la primera nota del modo, y funciona como centro modal: la casa a la que todo vuelve. Aporta sensación de descanso y estabilidad, con un carácter luminoso y abierto. Por sí solo suena como cualquier acorde mayor; el color lidio aparece cuando lo combinas con los acordes característicos del modo.",
            typicalUse = "Úsalo para abrir y cerrar tus progresiones, de modo que el oído sienta a F como casa. El vaivén I-II (F-G) es la fórmula lidia más típica para asentarlo.",
        ),
        "lydian.2" to DegreeFicha(
            explanation = "En una tonalidad mayor normal, el acorde del segundo grado sería menor; aquí es mayor gracias a B, la 4ª aumentada que define al modo lidio. Ese detalle convierte a G en un acorde característico, la verdadera marca del color del modo. Produce una sensación soñadora y flotante, muy asociada a las bandas sonoras de cine.",
            typicalUse = "El vaivén I-II (F-G) es el sonido lidio por excelencia: dos acordes mayores a un tono de distancia con F como casa. También funciona en progresiones como I-II-I o I-II-vii.",
        ),
        "lydian.3" to DegreeFicha(
            explanation = "Es la tríada menor construida sobre la tercera nota del modo. Se considera un acorde neutro: no contiene la nota que define al lidio, así que ni refuerza ni debilita el color del modo. Aporta un matiz suave y algo melancólico que da variedad a la progresión.",
            typicalUse = "Funciona bien como acorde intermedio entre los grados principales, por ejemplo en I-iii-II o I-II-iii.",
        ),
        "lydian.4" to DegreeFicha(
            explanation = "Es la tríada disminuida construida sobre B, la propia 4ª aumentada del modo, así que lleva el color lidio en su raíz y se considera un acorde característico. Suena tenso e inestable porque contiene el tritono de la escala madre, C mayor (las notas B y F). Ese tritono tiende a empujar el oído hacia C, así que conviene usarlo con cuidado y en dosis pequeñas para no perder a F como casa.",
            typicalUse = "Úsalo como acorde de paso breve entre acordes más estables, por ejemplo I-iv°-iii, y vuelve pronto a I para reafirmar el centro.",
        ),
        "lydian.5" to DegreeFicha(
            explanation = "Es la tríada mayor sobre la quinta nota del modo y se comporta como un acorde neutro y estable. Ten en cuenta que C es justamente el acorde principal de la escala madre de la que proviene F lidio: si lo usas demasiado o descansas en él, toda la progresión puede empezar a sonar a C mayor en lugar de a F lidio. Con moderación, aporta un sonido familiar y sólido.",
            typicalUse = "Úsalo de paso y regresa pronto a la casa para que F siga mandando, por ejemplo en I-V-I o I-V-II.",
        ),
        "lydian.6" to DegreeFicha(
            explanation = "Es la tríada menor construida sobre la sexta nota del modo. Es un acorde neutro: no contiene la nota característica, así que ni suma ni resta color lidio. Aporta un matiz más oscuro y emotivo que contrasta bien con el brillo de I y II.",
            typicalUse = "Sirve para dar profundidad entre los acordes principales, por ejemplo en I-vi-II o I-II-vi.",
        ),
        "lydian.7" to DegreeFicha(
            explanation = "Es la tríada menor sobre la séptima nota del modo y contiene B, la 4ª aumentada que le da su color al lidio; por eso se considera un acorde característico. Aunque suena suave y melancólico, mantiene viva la nota que distingue al modo. Es una forma más discreta de traer el color lidio que el acorde II.",
            typicalUse = "Funciona bien alternando con la casa o acompañando al II, por ejemplo en I-vii-I o I-II-vii.",
        ),
        "mixolydian.1" to DegreeFicha(
            explanation = "G es el centro modal, tu casa en el modo mixolidio: el acorde donde la música descansa, cumpliendo el papel de tónica (el punto de reposo). Es un acorde mayor, pero con un detalle clave: si le agregas la séptima de la escala se convierte directamente en G7, ese sonido con sabor a blues y rock que define al modo. Por eso la casa mixolidia se siente brillante y estable, pero con un punto de crudeza que la aleja de la escala mayor tradicional.",
            typicalUse = "Es el punto de partida y de regreso de casi cualquier progresión mixolidia. Aparece como base en vamps clásicos como I-VII (G-F) o I-IV (G-C), donde se queda sonando mientras los demás acordes le dan color.",
        ),
        "mixolydian.2" to DegreeFicha(
            explanation = "Am es el segundo grado, un acorde menor de carácter neutro dentro del modo. No contiene la nota característica del mixolidio, así que por sí solo no define el sonido del modo, pero tampoco lo contradice: aporta variedad y un matiz suave sin sacarte de casa.",
            typicalUse = "Sirve para dar movimiento entre acordes más definidos, por ejemplo en progresiones como I-ii-IV (G-Am-C) o ii-I (Am-G).",
        ),
        "mixolydian.3" to DegreeFicha(
            explanation = "B° es el tercer grado, un acorde disminuido, tenso e inestable. Es un acorde característico (color del modo) porque contiene la nota F, la séptima menor que le da su sonido al mixolidio. Además, entre B y F se forma un tritono, el mismo intervalo inquieto de la escala madre de C mayor, así que este acorde tiende a llevar el oído hacia C. Conviene dosificarlo: si lo usas demasiado puede debilitar la sensación de que G es la casa.",
            typicalUse = "Se usa poco y de paso, casi siempre como puente breve entre grados más estables, por ejemplo en movimientos como I-iii°-IV (G-B°-C).",
        ),
        "mixolydian.4" to DegreeFicha(
            explanation = "C es el cuarto grado, un acorde mayor estable y amable. Es justamente el acorde de la escala madre (C mayor), así que convive de forma muy natural con el resto del campo. Como no contiene la nota característica del modo, se considera neutro: acompaña bien sin robar protagonismo a la casa.",
            typicalUse = "Es uno de los compañeros favoritos del primer grado: el vamp I-IV (G-C) es un sonido clásico del rock y el funk. También aparece en progresiones como I-VII-IV (G-F-C).",
        ),
        "mixolydian.5" to DegreeFicha(
            explanation = "Dm es el quinto grado, y aquí hay una gran diferencia con la tonalidad mayor clásica: es un acorde menor en lugar de mayor. Eso ocurre porque contiene la nota F, la séptima menor característica del modo, que vuelve menor a este acorde. Escuchar un quinto grado menor es una de las pistas más claras de que estás en mixolidio y no en una tonalidad mayor tradicional. Su sonido es suave y algo melancólico, sin la tensión típica del acorde de dominante (el acorde de tensión) de la tonalidad mayor.",
            typicalUse = "Aporta color modal en progresiones como I-v (G-Dm) o I-v-IV (G-Dm-C), frecuentes en rock y folk.",
        ),
        "mixolydian.6" to DegreeFicha(
            explanation = "Em es el sexto grado, un acorde menor de carácter neutro. No lleva la nota característica del modo, así que ni refuerza ni contradice el sonido mixolidio: simplemente suma un color menor y algo nostálgico que contrasta con la casa mayor.",
            typicalUse = "Funciona bien como contraste dentro de progresiones como I-vi-IV (G-Em-C) o vi-VII-I (Em-F-G).",
        ),
        "mixolydian.7" to DegreeFicha(
            explanation = "F es el séptimo grado y el sello del modo mixolidio: un acorde mayor construido directamente sobre F, la séptima menor de la escala, la nota que da al modo su carácter. Muchos guitarristas lo apodan bVII, porque al compararlo con la escala mayor su fundamental queda un semitono más abajo de lo esperado; aquí lo numeramos VII porque los grados se cuentan sobre la escala que estamos usando. En cuanto suena junto a la casa, el color mixolidio aparece de inmediato.",
            typicalUse = "Su uso estrella es el vamp I-VII (G-F), un clásico del rock. También brilla en progresiones como I-VII-IV (G-F-C).",
        ),
        "locrian.1" to DegreeFicha(
            explanation = "B° es el primer grado y el centro modal del locrio: la casa, el punto al que todo debería regresar. Lo único de este modo es que su casa es un acorde disminuido, inestable por naturaleza, porque contiene la quinta disminuida (F) dentro de su propia estructura. Eso significa que el descanso nunca se siente del todo resuelto, y por eso el locrio se considera un modo más teórico que práctico. Sostener a B° como verdadero centro exige insistir mucho en él, tanto en el bajo como en la melodía.",
            typicalUse = "Se usa en vamps cortos que giran alrededor de la casa, por ejemplo i°-II-i° o i°-iv, repitiendo B° para que el oído lo acepte como referencia. Su sonido oscuro y tenso lo hace más común en músicas de atmósfera densa que en canciones convencionales.",
        ),
        "locrian.2" to DegreeFicha(
            explanation = "C es el segundo grado del campo armónico de B locrio, una tríada mayor construida sobre la 2ª menor del modo, a solo medio tono de la casa. Por esa cercanía también aporta color modal, un sabor que el locrio comparte con el frigio; aun así, la nota exclusiva que define al locrio es la 5ª disminuida (F), y este acorde no la contiene, por eso la app lo clasifica como neutro. Al ser un acorde mayor y estable, ofrece un contraste luminoso frente a la inestabilidad de la casa (B°). Eso sí, ten cuidado: si le das demasiado protagonismo, el oído tiende a escucharlo como centro y la sensación locria se pierde.",
            typicalUse = "Funciona como acorde de paso o de contraste en progresiones modales, por ejemplo i°-II-i° o II-i°, regresando siempre a B° para no perder la referencia.",
        ),
        "locrian.3" to DegreeFicha(
            explanation = "Dm es el tercer grado del campo armónico de B locrio y uno de sus acordes característicos, es decir, de los que aportan el color del modo. Lo que lo hace especial es que contiene la nota F, la quinta disminuida del modo, responsable del sonido oscuro e inestable del locrio. A diferencia de la casa, Dm es una tríada menor común y estable, así que te permite mostrar ese color sin tanta tensión.",
            typicalUse = "Úsalo cerca de la casa para reforzar el sabor del modo, por ejemplo en i°-iii-i° o iii-i°, cuidando que B siga sonando como nota de referencia.",
        ),
        "locrian.4" to DegreeFicha(
            explanation = "Em es el cuarto grado del campo armónico de B locrio, una tríada menor que funciona como acorde neutro: encaja bien en el modo, pero no contiene la quinta disminuida que lo define. Aporta un color menor suave, útil para dar variedad y movimiento sin robarle protagonismo a los acordes característicos.",
            typicalUse = "Se usa como acorde de paso o de relleno en progresiones modales, por ejemplo i°-iv-V o iv-i°.",
        ),
        "locrian.5" to DegreeFicha(
            explanation = "F es el quinto grado del campo armónico de B locrio y quizá su acorde más característico, porque está construido directamente sobre la quinta disminuida del modo, la nota F. Aunque en el sistema mayor el quinto grado suele cumplir la función de dominante o tensión que empuja hacia el descanso, aquí no ocurre eso: F está a distancia de quinta disminuida de B, una relación muy inusual que es la esencia del color locrio. Además, es una tríada mayor estable en sí misma, y ese contraste con la casa disminuida ayuda a definir el modo.",
            typicalUse = "Alternarlo con la casa es la manera más directa de hacer sonar el locrio, por ejemplo i°-V-i° o i°-iv-V, manteniendo B° como punto de regreso.",
        ),
        "locrian.6" to DegreeFicha(
            explanation = "G es el sexto grado del campo armónico de B locrio, una tríada mayor de función neutra. No incluye la nota característica del modo, así que aporta brillo y estabilidad general sin definir por sí solo el sonido locrio. Sirve para abrir la progresión y dar un respiro antes de volver a la tensión de la casa.",
            typicalUse = "Funciona bien como acorde de paso o de contraste, por ejemplo en i°-VI-V o VI-i°, regresando siempre a B°.",
        ),
        "locrian.7" to DegreeFicha(
            explanation = "Am es el séptimo grado del campo armónico de B locrio, una tríada menor de carácter neutro. Comparte la atmósfera oscura general del modo, pero no contiene su nota característica, así que funciona sobre todo como acorde de acompañamiento. Como está un tono por debajo de la casa, puede conducir hacia B° con un movimiento ascendente bastante natural.",
            typicalUse = "Pruébalo justo antes de la casa, por ejemplo en vii-i° o i°-vii-i°, para crear un regreso suave a B°.",
        ),
    )

    val scaleFichas: Map<ScaleType, ScaleFicha> = mapOf(
        ScaleType.MAJOR to ScaleFicha(
            name = "Mayor",
            description = "Es la escala de referencia de la música occidental: estable, brillante y familiar. Todo el modelo de funciones parte de ella, y sus siete acordes cubren las tres sensaciones: Tónica (Descanso), Subdominante (De paso) y Dominante (Tensión). Si entiendes su lógica, el resto del círculo de quintas se vuelve mucho más fácil de leer.",
            tip = "Toca I-IV-V en C (C, F, G) y escucha cómo G pide volver a C. Ese ciclo de tensión y descanso es la base de casi todo lo que sigue.",
        ),
        ScaleType.NATURAL_MINOR to ScaleFicha(
            name = "Menor natural",
            description = "Es la cara melancólica del sistema: comparte notas con su relativa mayor, pero descansa en otro centro. Como su 7º grado está a un tono entero de la tónica, no tiene sensible, y por eso sus tensiones son más suaves y menos urgentes. El resultado es un sonido introspectivo, muy común en baladas, rock y pop.",
            tip = "Prueba i-VI-VII en A menor (Am, F, G) para sentir su color. Si notas que el v menor (Em) empuja poco hacia la tónica, es normal: para eso existe la menor armónica.",
        ),
        ScaleType.HARMONIC_MINOR to ScaleFicha(
            name = "Menor armónica",
            description = "Es la menor natural con la 7ª elevada medio tono. Ese cambio crea una sensible y convierte el V en un acorde mayor de verdad (E o E7 en A menor), con una Dominante (Tensión) que sí pide resolver. A cambio, queda un salto de tono y medio entre el 6º y el 7º grado, responsable de su sonido exótico tan reconocible.",
            tip = "Ten en cuenta que sus notas no forman un arco contiguo en el círculo de quintas, a diferencia de la mayor y los modos. Úsala sobre todo cuando suene el V mayor, por ejemplo en i-iv-V (Am, Dm, E7).",
        ),
        ScaleType.MELODIC_MINOR to ScaleFicha(
            name = "Menor melódica ascendente",
            description = "Es la menor con la 6ª y la 7ª elevadas al subir. Nació para suavizar el salto de tono y medio de la armónica: conserva la sensible y el V mayor, pero el camino melódico hacia la tónica queda más fluido, casi como en la mayor.",
            tip = "Igual que la armónica, sus notas no forman un arco contiguo en el círculo de quintas. Pruébala en líneas que ascienden hacia la tónica mientras suena el V mayor.",
        ),
        ScaleType.IONIAN to ScaleFicha(
            name = "Jónico",
            description = "Es la escala mayor vista como modo: mismas notas y mismo centro, solo cambia el nombre. Por eso en esta app se analiza con la lente tonal, es decir, con el modelo de funciones de la escala mayor. Su sensación es la misma: estable, brillante y en casa.",
            tip = "Trátalo exactamente como la escala mayor: progresiones como I-IV-V o I-vi-IV-V funcionan tal cual.",
        ),
        ScaleType.DORIAN to ScaleFicha(
            name = "Dórico",
            description = "Es un modo menor con la 6ª mayor, su nota característica. Ese único cambio respecto a la menor natural le da un color menor más luminoso y menos triste, muy presente en el funk, el rock y el jazz modal. Piensa en un menor que sonríe un poco.",
            tip = "Sostén el centro modal con un vamp i-IV (Dm-G en D dórico), que resalta la 6ª mayor, y dosifica los acordes que empujan hacia el mayor relativo (C) para no perder el centro.",
        ),
        ScaleType.PHRYGIAN to ScaleFicha(
            name = "Frigio",
            description = "Es un modo menor con la 2ª menor, su nota característica, a solo medio tono de la tónica. Ese roce tan cercano le da un sonido oscuro con sabor español, asociado al flamenco y muy usado también en el metal. Es de los modos más fáciles de reconocer de oído.",
            tip = "Sostén el centro modal con un vamp i-bII (Em-F en E frigio), que deja oír su nota característica, y dosifica los acordes que tiran hacia el mayor relativo (C) para que la tónica siga sonando como casa.",
        ),
        ScaleType.LYDIAN to ScaleFicha(
            name = "Lidio",
            description = "Es un modo mayor con la 4ª aumentada, su nota característica. Al sustituir la 4ª justa, pierde peso y gana un sonido flotante y soñador, muy usado en bandas sonoras. Suena mayor, pero como suspendido en el aire.",
            tip = "Sostén el centro modal con un vamp I-II (F-G en F lidio), que muestra la 4ª aumentada, y dosifica los acordes que empujan hacia el mayor relativo (C) para que la tónica no pierda protagonismo.",
        ),
        ScaleType.MIXOLYDIAN to ScaleFicha(
            name = "Mixolidio",
            description = "Es un modo mayor con la 7ª menor, su nota característica. Suena mayor, pero más terroso y relajado que la escala mayor: es el sonido de muchísimo rock y blues. Si has tocado riffs sobre acordes mayores con séptima menor, ya lo tienes en los dedos.",
            tip = "Sostén el centro modal con un vamp I-bVII (G-F en G mixolidio) y dosifica los acordes que tiran hacia el mayor relativo (C), para que la tónica del modo se mantenga como centro.",
        ),
        ScaleType.AEOLIAN to ScaleFicha(
            name = "Eólico",
            description = "Es la menor natural vista como modo: mismas notas y mismo centro, solo cambia la etiqueta. Por eso en esta app se analiza con la lente tonal, con el modelo de funciones del contexto menor. Su sensación es la misma: melancólica, introspectiva y de tensiones suaves.",
            tip = "Trátalo exactamente como la menor natural: progresiones como i-VI-VII o i-iv-v funcionan tal cual.",
        ),
        ScaleType.LOCRIAN to ScaleFicha(
            name = "Locrio",
            description = "Es el modo más inestable: tiene la 2ª menor y, sobre todo, la 5ª disminuida, así que su acorde de centro es disminuido y nunca llega a sonar como un verdadero Descanso. Por eso es más un objeto teórico que una herramienta práctica, aunque completa el mapa de los siete modos. Conocerlo te ayuda a entender por qué los demás sí funcionan.",
            tip = "Si quieres experimentar, sostén el centro modal todo lo posible (por ejemplo con Bm7b5 en B locrio) y dosifica al máximo los acordes que tiran hacia el mayor relativo (C), porque aquí casi todo empuja fuera del centro.",
        ),
        ScaleType.PENT_MAJOR to ScaleFicha(
            name = "Pentatónica mayor",
            description = "Son cinco notas de la escala mayor (grados 1, 2, 3, 5 y 6), elegidas de modo que no queda ningún semitono entre ellas. Sin notas de roce, es muy difícil que suene mal: por eso es la aliada perfecta para empezar a improvisar. Su carácter es abierto, brillante y amable.",
            tip = "En la app se muestra como una capa (overlay) sobre su escala madre mayor: los acordes y las funciones que ves son los de la madre. Úsala con confianza sobre progresiones como I-IV-V o I-V-vi-IV.",
        ),
        ScaleType.PENT_MINOR to ScaleFicha(
            name = "Pentatónica menor",
            description = "Son cinco notas de la menor natural (grados 1, 3, 4, 5 y 7), también sin semitonos entre ellas. Es la escala del rock y del blues, y probablemente la primera que aprende cualquier guitarrista: directa, expresiva y muy difícil de hacer sonar mal.",
            tip = "Igual que la pentatónica mayor, se muestra como overlay sobre su escala madre, la menor natural: los acordes y las funciones son los de la madre. Pruébala sobre i-VII-VI (Am, G, F) o sobre casi cualquier progresión menor.",
        ),
    )

    const val functionModelIntro: String =
        "El modelo de funciones es un mapa pedagógico: una forma útil de ordenar lo que escuchas, no una verdad absoluta ni la única manera de analizar la música. La idea central es que cada acorde de la escala genera una de tres sensaciones básicas. La Tónica (Descanso) es el punto de llegada: ahí la música se siente en casa. La Subdominante (De paso) crea movimiento: te aleja del descanso y prepara lo que viene, sin exigir nada. La Dominante (Tensión) genera una inestabilidad que pide resolver, casi siempre de vuelta a la Tónica, como en V-I o en el clásico ii-V-I. Algunos grados son ambiguos y pueden cumplir más de un papel según el contexto; la app los marca de forma distinta para recordártelo."

    const val cautionExplanation: String =
        "La etiqueta de precaución aparece cuando una nota agregada forma una 9ª menor con alguna nota del acorde, un intervalo que produce un choque especialmente áspero. El caso típico es la 11ª natural sobre un acorde con 3ª mayor: en C, la nota F roza con la 3ª del acorde (E). No ocultamos estos acordes, porque escuchar el choque también enseña y entrena el oído. En la práctica esa nota suele omitirse: por eso un acorde de 13, como C13, se toca normalmente sin la 11ª."

    const val modalTensionNote: String =
        "Los acordes de tensión que ves aquí contienen el tritono de la escala madre, y por eso empujan hacia el mayor relativo en vez de reforzar tu centro modal. Dosifícalos: usados con medida dan dirección, pero si abusas de ellos el modo se diluye en su relativa mayor."
}
