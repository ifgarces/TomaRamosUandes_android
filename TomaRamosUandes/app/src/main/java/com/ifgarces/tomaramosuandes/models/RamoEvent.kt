package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.utils.SpanishToStringOf
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


/**
 * Models the event type for a `Ramo`.
 * @property ID Primary key.
 * @property ramoNRC Foreignt key referencing the `Ramo` this event belongs to.
 * @property type Represents the type (assistenship, class, laboratory, tutorial, test or exam).
 * @property location Location for the event (room).
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
    val location  :String,
    val dayOfWeek :DayOfWeek,
    val startTime :LocalTime,
    val endTime   :LocalTime,
    var date      :LocalDate?
) {
    companion object {
        const val TABLE_NAME :String = "ramo_event"

        /**
         * Needed for interaction with Firebase, as `LocalTime` and `LocalDate` cannot be uploaded
         * without some kind of serialization.
         * @param event The `RamoEvent` instance to be converted.
         * @return A map with the attributes as strings and the values accordingly, but dates and
         * times are converted to string.
         */
        public fun toRawMap(event :RamoEvent) :Map<String, Any?> {
            return mapOf<String, Any?>(
                "ID" to event.ID,
                "ramoNRC" to event.ramoNRC,
                "location" to event.location,
                "type" to event.type,
                "dayOfWeek" to event.dayOfWeek,
                "startTime" to event.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))!!,
                "endTime" to event.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))!!,
                "date" to if (event.date == null) null else event.date!!.format(DateTimeFormatter.ISO_DATE)!!
            )
        }

        /**
         * Deserializes.
         * @param map The map to be converted back to a `RamoEvent` instance.
         */
        public fun fromRawMap(map :Map<String, Any?>) :RamoEvent {
            return RamoEvent(
                ID = map["ID"]!!.toString().toDouble().toInt(), // for some reason, need to cast to double and only then to integer
                ramoNRC = map["ramoNRC"]!!.toString().toDouble().toInt(),
                type = map["type"]!!.toString().toDouble().toInt(),
                location = map["location"]!!.toString(),
                dayOfWeek = DayOfWeek.valueOf(map["dayOfWeek"]!!.toString()),
                startTime = LocalTime.parse(
                    map["startTime"]!!.toString(), DateTimeFormatter.ofPattern("HH:mm")
                ),
                endTime = LocalTime.parse(
                    map["endTime"]!!.toString(), DateTimeFormatter.ofPattern("HH:mm")
                ),
                date = if (map["date"] == null) null else LocalDate.parse(
                    map["date"]!!.toString(), DateTimeFormatter.ISO_DATE
                )
            )
        }
    }

    public fun toLargeString() :String {
        val dateOrDay :String =
            if (this.isEvaluation()) { this.date.toString() } // TODO: make sure to use "dd/MM/yyyy" format
            else { SpanishToStringOf.dayOfWeek(this.dayOfWeek) }
        return """\
Tipo: ${SpanishToStringOf.ramoEventType(eventType=this.type, shorten=false)!!}
Ramo: ${DataMaster.findRamo(NRC=this.ramoNRC, searchInUserList=false)!!.nombre} (NRC ${this.ramoNRC})
Fecha: ${dateOrDay} (${this.startTime} - ${this.endTime})
Sala: ${if (this.location != "") this.location else "(no informada)"}""".multilineTrim()
    }

    /**
     * Single line and short `toString()` method. Used in event conflict reports.
     */
    public fun toShortString() :String {
        val dateOrDay :String =
            if (this.isEvaluation()) { this.date.toString() }
            else { SpanishToStringOf.dayOfWeek(this.dayOfWeek) }
        return """\
${SpanishToStringOf.ramoEventType(eventType=this.type, shorten=false)!!}: ${DataMaster.findRamo(NRC=this.ramoNRC, searchInUserList=false)!!.nombre}
(${dateOrDay} ${this.startTime} - ${this.endTime})
Sala: ${if (this.location != "") this.location else "(no informada)"}""".multilineTrim().format(
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
    public fun isEvaluation() :Boolean {
        return (this.type == RamoEventType.PRBA) || (this.type == RamoEventType.EXAM)
    }
}
