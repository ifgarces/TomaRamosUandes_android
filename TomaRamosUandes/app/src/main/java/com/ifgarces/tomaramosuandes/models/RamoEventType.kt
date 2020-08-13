package com.ifgarces.tomaramosuandes.models


object RamoEventType {
    const val CLAS :Int = 0 // clase(s)
    const val AYUD :Int = 1 // ayudantía(s)
    const val LABT :Int = 2 // laboratorio(s)
    const val TUTR :Int = 3 // tutorial(es)
    const val PRBA :Int = 4 // prueba(s)
    const val EXAM :Int = 5 // examen

    fun toStringOf(eventType :Int) : String? {
        return when(eventType) {
            this.CLAS -> "CLASE"
            this.AYUD -> "AYUD"
            this.LABT -> "LABT"
            this.TUTR -> "TUTR"
            this.PRBA -> "PRBA"
            this.EXAM -> "EXAM"
            else      -> null
        }
    }
}