package com.ifgarces.tomaramosuandes.utils

import com.ifgarces.tomaramosuandes.models.RamoEventType
import java.time.DayOfWeek
import java.time.LocalDate


/**
 * Language conversion tool for `toString()`-like methods of varius types.
 */
object SpanishToStringOf {

    /**
     * Returns the spanish string equivalent for `day`.
     */
    public fun dayOfWeek(day :DayOfWeek) : String {
        return when(day) {
            DayOfWeek.MONDAY    -> "Lunes"
            DayOfWeek.TUESDAY   -> "Martes"
            DayOfWeek.WEDNESDAY -> "Miércoles"
            DayOfWeek.THURSDAY  -> "Jueves"
            DayOfWeek.FRIDAY    -> "Viernes"
            DayOfWeek.SATURDAY  -> "Sábado"
            DayOfWeek.SUNDAY    -> "Domingo"
        }
    }

    /**
     * Returns string date with chilean format: "dd/MM/yyyy".
     */
    public fun localDate(date :LocalDate) : String {
        val aux :List<String> = date.toString().split("-") // ~ "yyyy-MM-dd"
        return "${aux[2]}/${aux[1]}/${aux[0]}" // "dd/MM/yyyy"
    }

    /**
     * Returns string equivalent for the given integer event type.
     * @param eventType The integer `RamoEventType`.
     * @param shorten If true, will return a shortened upper-cased string equivalent.
     */
    public fun ramoEventType(eventType :Int, shorten :Boolean) : String? {
        return when(eventType) {
            RamoEventType.CLAS -> if (shorten) {"CLASE"} else {"Clase"}
            RamoEventType.AYUD -> if (shorten) {"AYUD"}  else {"Ayudantía"}
            RamoEventType.LABT -> if (shorten) {"LABT"}  else {"Laboratorio"}
            RamoEventType.TUTR -> if (shorten) {"TUTR"}  else {"Tutorial"}
            RamoEventType.PRBA -> if (shorten) {"PRBA"}  else {"Prueba"}
            RamoEventType.EXAM -> if (shorten) {"EXAM"}  else {"Examen"}
            else               -> null // invalid type, very invalid type.
        }
    }
}