package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


/* Modela un evento de ramo (ayudantía, clase, laboratorio, tutorial, prueba o examen) */
@Entity(tableName=RamoEvent.TABLE_NAME)
data class RamoEvent(
    @PrimaryKey(autoGenerate=false) val ID :Int,
    val ramoNRC   :Int, // (foreign key) referencia al ramo al que pertenece el evento
    val type      :Int,
    val dayofWeek :DayOfWeek,
    val startTime :LocalTime,
    val endTime   :LocalTime,
    var date      :LocalDate?

) {
    companion object { const val TABLE_NAME :String = "ramo_event" }
}