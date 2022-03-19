package com.ifgarces.tomaramosuandes.models


/**
 * Holds the representation for every `RamoEvent` type. Not an enum class because it would cause
 * trouble when storing with Room database. Meh.
 */
object RamoEventType {
    const val CLAS :Int = 0 // clase
    const val AYUD :Int = 1 // ayudantía
    const val LABT :Int = 2 // laboratorio
    const val TUTR :Int = 3 // tutorial
    const val PRBA :Int = 4 // prueba
    const val EXAM :Int = 5 // examen
}