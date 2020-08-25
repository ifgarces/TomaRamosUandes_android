package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.utils.SpanishStringer
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


/* Modela un evento de ramo (ayudantía, clase, laboratorio, tutorial, prueba o examen) */
@Entity(tableName=RamoEvent.TABLE_NAME)
data class RamoEvent(
    @PrimaryKey(autoGenerate=false) val ID :Int,
    val ramoNRC   :Int, // (foreign key) referencia al ramo al que pertenece el evento
    val type      :Int,
    val dayOfWeek :DayOfWeek,
    val startTime :LocalTime,
    val endTime   :LocalTime,
    var date      :LocalDate?
) {
    companion object {
        const val TABLE_NAME :String = "ramo_event"
    }

    public fun toLargeString() : String {
        val dateOrDay :String =
            if (this.isEvaluation()) { this.date.toString() } // TODO: make sure to use "dd/MM/yyyy" format
            else { SpanishStringer.dayOfWeek(this.dayOfWeek) }
        return """
                Tipo: %s
                Ramo: %s (NRC %d)
                Fecha: %s (%s - %s)
            """.multilineTrim().format(
            SpanishStringer.ramoEventType(eventType=this.type, shorten=false)!!,
            DataMaster.findRamo(NRC=this.ramoNRC)!!.nombre,
            this.ramoNRC,
            dateOrDay,
            this.startTime, // TODO: make sure to use "HH:mm" format
            this.endTime
        )
    }

    /* Single line and short `toString()` method. Used in event conflict reports. */
    public fun toShortString() : String {
        val dateOrDay :String =
            if (this.isEvaluation()) { this.date.toString() }
            else { SpanishStringer.dayOfWeek(this.dayOfWeek) }
        return "%s: %s\n\t\t(%s %s-%s)".format(
            SpanishStringer.ramoEventType(eventType=this.type, shorten=false)!!,
            DataMaster.findRamo(NRC=this.ramoNRC)!!.nombre,
            dateOrDay,
            this.startTime,
            this.endTime
        )
    }

    /* Checks if the event is a test or exam */
    public fun isEvaluation() : Boolean {
        return (this.type == RamoEventType.PRBA) || (this.type == RamoEventType.EXAM)
    }
}