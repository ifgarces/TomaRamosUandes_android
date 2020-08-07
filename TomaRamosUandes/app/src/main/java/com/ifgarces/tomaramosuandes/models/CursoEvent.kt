package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


/* Modela un evento de ramo (ayudantía, clase, laboratorio, tutorial, prueba o exámen) */
@Entity(tableName=CursoEvent.TABLE_NAME)
data class CursoEvent(
    @PrimaryKey(autoGenerate=false)  val ID :Int,
    val cursoNRC  :Int, // (foreign key) referencia al curso al que pertenece el evento
    val day       :DayOfWeek,
    val startTime :LocalTime,
    val endTime   :LocalTime,
    var date      :LocalDate? = null // <- sólo está presente en pruebas exámenes.
    // val location :String // <- cuando no haya pandemia, incluye la sala de eventos físicos

) {
    companion object { const val TABLE_NAME :String = "curso_event" }
}