package com.ifgarces.tomaramosuandes.utils

import java.time.DayOfWeek
import java.time.LocalDate


/**
 * Language conversion tool for `toString()`-like methods of varius types.
 */
object SpanishFormatter {

    public fun dayOfWeek(day :DayOfWeek) : String {
        return when(day) {
            DayOfWeek.MONDAY    -> { "Lunes" }
            DayOfWeek.TUESDAY   -> { "Martes" }
            DayOfWeek.WEDNESDAY -> { "Miércoles" }
            DayOfWeek.THURSDAY  -> { "Jueves" }
            DayOfWeek.FRIDAY    -> { "Viernes" }
            DayOfWeek.SATURDAY  -> { "Sábado" }
            DayOfWeek.SUNDAY    -> { "Domingo" }
        }
    }

    /* Returns stringed date with format "dd/MM/yyyy" */
    public fun localDate(date :LocalDate) : String {
        val aux :List<String> = date.toString().split("-") // format: "MM-dd-yyyy"
        return "${aux[1]}/${aux[0]}/${aux[2]}"
    }
}