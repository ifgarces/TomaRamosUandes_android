package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.annotations.JsonAdapter
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.utils.SpanishToStringOf
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


/**
 * Models the event type for a `Ramo`.
 * @property ID Primary key.
 * @property ramoNRC Foreignt key referencing the `Ramo` this event belongs to.
 * @property type Represents the type (assistenship, class, laboratory, tutorial, test or exam).
 * @property dayOfWeek Day it takes place, when it is weekly repetitive, like a class.
 * @property startTime Starting time (block start).
 * @property endTime Finish time (block end).
 * @property date This will be not null only when the date makes sense depending on the type: will be
 * non-null only for evaluations (tests and exams).
 */
@Entity(tableName=RamoEvent.TABLE_NAME)
data class RamoEvent(
    @PrimaryKey(autoGenerate=false) val ID :Int,
    val ramoNRC   :Int,
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
            else { SpanishToStringOf.dayOfWeek(this.dayOfWeek) }
        return """
                Tipo: %s\
                Ramo: %s (NRC %d)\
                Fecha: %s (%s - %s)
            """.multilineTrim().format(
            SpanishToStringOf.ramoEventType(eventType=this.type, shorten=false)!!,
            DataMaster.findRamo(NRC=this.ramoNRC, searchInUserList=false)!!.nombre,
            this.ramoNRC,
            dateOrDay,
            this.startTime, // TODO: make sure to use "HH:mm" format
            this.endTime
        )
    }

    /**
     * Single line and short `toString()` method. Used in event conflict reports.
     */
    public fun toShortString() : String {
        val dateOrDay :String =
            if (this.isEvaluation()) { this.date.toString() }
            else { SpanishToStringOf.dayOfWeek(this.dayOfWeek) }
        return "%s: %s\n\t\t(%s %s-%s)".format(
            SpanishToStringOf.ramoEventType(eventType=this.type, shorten=false)!!,
            DataMaster.findRamo(NRC=this.ramoNRC, searchInUserList=false)!!.nombre,
            dateOrDay,
            this.startTime,
            this.endTime
        )
    }

    /**
     * Checks if the event is a test or exam
     */
    public fun isEvaluation() : Boolean {
        return (this.type == RamoEventType.PRBA) || (this.type == RamoEventType.EXAM)
    }
}
