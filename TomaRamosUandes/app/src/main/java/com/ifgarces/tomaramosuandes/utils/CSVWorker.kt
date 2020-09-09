package com.ifgarces.tomaramosuandes.utils

import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


/* Handles CSV parsing */
object CSVWorker {
    private const val EXPLICIT_COMMA_MARKER :String = ";;" // used to have elements like "element with;; comma" instead of "\"element with, comma\"" (harder to parse)
    private const val TIME_SEPARATOR :String = "-" // e.g. "10:30 - 12:20"
    private val       date_format = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("ES", "ES"))
    private val       time_format = DateTimeFormatter.ISO_TIME

    /* Each line of the CSV file describes an event. These are the columns and their index: */
    private val csv_columns = object {
        val PLAN_ESTUDIOS :Int = 0
        val NRC           :Int = 1
        val CONECTOR_LIGA :Int = 2
        val LISTA_CRUZADA :Int = 3
        val MATERIA       :Int = 4
        val CURSONUM      :Int = 5
        val SECCIÓN       :Int = 6
        val NOMBRE        :Int = 7
        val CRÉDITO       :Int = 8
        val LUNES         :Int = 9
        val MARTES        :Int = 10
        val MIÉRCOLES     :Int = 11
        val JUEVES        :Int = 12
        val VIERNES       :Int = 13
        val FECHA_INICIO  :Int = 14
        val FECHA_FIN     :Int = 15 // <- UNUSED
        val TIPO_EVENTO   :Int = 16
        val PROFESOR      :Int = 17

        fun toDayOfWeek(column :Int) : DayOfWeek? {
            return when(column) {
                9 -> DayOfWeek.MONDAY
                10 -> DayOfWeek.TUESDAY
                11 -> DayOfWeek.WEDNESDAY
                12 -> DayOfWeek.THURSDAY
                13 -> DayOfWeek.FRIDAY
                else -> null
            }
        }
    }
    private val event_types = object {
        val CLASE       :String = "OLIN"
        val AYUDANTÍA   :String = "AYON"
        val LABORATORIO :String = "LBON"
        val TUTORIAL    :String = "TUTR"
        val PRUEBA      :String = "PRON"
        val EXAMEN      :String = "EXON"
    }


    /**
     * Converts the contents of the CSV (holding the period catalog) into a collection of `Ramo` and `RamoEvent`.
     * On fatal parsing error (invalid `csv_lines`), returns null.
     */
    public fun parseCSV(csv_lines :List<String>) : Pair<List<Ramo>, List<RamoEvent>>? {

        // [!] ---
        // [!] TODO: fix last row not being successfully parsed
        // [!] ---

        val catalogResult :Pair< MutableList<Ramo>, MutableList<RamoEvent> >
                = Pair(mutableListOf(), mutableListOf()) // ~ (ramos, events)

        val NRCs :MutableList<Int> = mutableListOf()
        var line :List<String> = listOf()
        val current = object { // buffer-like object, temporal data holder
            var NRC   :Int = 0
            var curso :Int = 0
            lateinit var dayOfWeek :DayOfWeek
            lateinit var eventType :String
            lateinit var startTime :LocalTime
            lateinit var endTime   :LocalTime
            var date   :LocalDate? = null
            var events :MutableList<RamoEvent>? = null
        }
        var stringAux :String
        var splitterAux :List<String>
        var eventTypeAux :Int
        var eventsCounter :Int = 0
        var lineNum :Int = 0
        var badNRCsCount :Int = 0

        while (lineNum++ < csv_lines.count()-1) {
            line = csv_lines[lineNum].split(",")

            try {
                current.NRC = line[csv_columns.NRC].toInt()
            }
            catch (e :NumberFormatException) {
                Logf("[CSVWorker] Warning: NRC could not be parsed to integer at CSV line %d '%s'. %s. Assuming NRC assign is yet pending and assigning negative one.", lineNum+1, csv_lines[lineNum], e)
                current.NRC = -(badNRCsCount++)
            }

            try {
                current.curso = line[csv_columns.CURSONUM].toInt()
            }
            catch (e :NumberFormatException) {
                Logf("[CSVWorker] Warning: CursoNum could not be parsed to integer at CSV line %d: '%s'. %s. Assuming CursoNum assign is yet pending and assigning zero.", lineNum+1, csv_lines[lineNum], e)
                current.curso = 0
            }


            if (! NRCs.contains(current.NRC)) { // NUEVO `RAMO`
                // finishing parsing of previus `Ramo`
                if (catalogResult.first.count() > 0) {
                    catalogResult.second.addAll(current.events!!)
                }

                // preparing parsing of new `Ramo`
                NRCs.add(current.NRC)
                current.events = mutableListOf()
                try {
                    catalogResult.first.add(
                        Ramo(
                            NRC = current.NRC,
                            nombre = line[csv_columns.NOMBRE].trim(),
                            profesor = line[csv_columns.PROFESOR].trim(),
                            créditos = line[csv_columns.CRÉDITO].toInt(),
                            materia = line[csv_columns.MATERIA].trim()
                                .replace(EXPLICIT_COMMA_MARKER, ","),
                            curso = current.curso,
                            sección = line[csv_columns.SECCIÓN].trim()
                                .replace(EXPLICIT_COMMA_MARKER, ","),
                            planEstudios = line[csv_columns.PLAN_ESTUDIOS].trim()
                                .replace(EXPLICIT_COMMA_MARKER, ","),
                            conectLiga = line[csv_columns.CONECTOR_LIGA].trim()
                                .replace(EXPLICIT_COMMA_MARKER, ","),
                            listaCruzada = line[csv_columns.LISTA_CRUZADA].trim()
                                .replace(EXPLICIT_COMMA_MARKER, ",")
                        )
                    )
                }
                catch (e :NumberFormatException) { // `String.toInt()` error
                    Logf("[CSVWorker] Error: integer cast exception at CSV line %d: '%s'. %s", lineNum+1, csv_lines[lineNum], e)
                    return null
                }

            }

            // getting day of week and time interval of the event
            for ( column :Int in (csv_columns.LUNES .. csv_columns.VIERNES) ) {
                stringAux = line[column].replace(" ", "")
                if (stringAux != "") {
                    current.dayOfWeek = csv_columns.toDayOfWeek(column)!!
                    splitterAux = stringAux.split(TIME_SEPARATOR)
                    if (splitterAux.count() != 2) {
                        Logf("[CSVWorker] Error: unexpected split result on row %d: '%s'. Could not get time interval for event.", lineNum+1, line)
                        return null
                    }

                    try {
                        current.startTime = LocalTime.parse(splitterAux[0],
                            time_format
                        )
                        current.endTime = LocalTime.parse(splitterAux[1],
                            time_format
                        )
                    }
                    catch (e :DateTimeParseException) {
                        Logf("[CSVWorker] Error: Time parsing exception at CSV line %d: '%s'. %s", lineNum+1, csv_lines[lineNum], e)
                        return null
                    }
                    break
                }
            }

            current.eventType = line[csv_columns.TIPO_EVENTO].spanishUpperCase().replace(" ", "")

            when (current.eventType.replace(regex=Regex("[0-9 ]"), replacement="")) { // "PRON1" -> "PRON", etc.
                event_types.CLASE       -> { eventTypeAux = RamoEventType.CLAS }
                event_types.AYUDANTÍA   -> { eventTypeAux = RamoEventType.AYUD }
                event_types.LABORATORIO -> { eventTypeAux = RamoEventType.LABT }
                event_types.TUTORIAL    -> { eventTypeAux = RamoEventType.TUTR }
                event_types.PRUEBA      -> { eventTypeAux = RamoEventType.PRBA }
                event_types.EXAMEN      -> { eventTypeAux = RamoEventType.EXAM }
                else -> {
                    Logf("[CSVWorker] Error at CSV line %d: '%s'. Unknown event type '%s'", lineNum+1, line, current.eventType)
                    return null
                }
            }

            // including date only if it is a test/exam, assigning null otherwise
            if (eventTypeAux == RamoEventType.PRBA || eventTypeAux == RamoEventType.EXAM) {
                try {
                    current.date = LocalDate.parse( line[csv_columns.FECHA_INICIO], date_format )
                }
                catch (e :DateTimeParseException) {
                    Logf("[CSVWorker] Error: Date parsing exception at CSV line %d: '%s'. %s", lineNum+1, csv_lines[lineNum], e)
                    return null
                }
            }
            else {
                current.date = null
            }

            current.events!!.add(
                RamoEvent(
                    ID = eventsCounter++,
                    type = eventTypeAux,
                    ramoNRC = current.NRC,
                    dayOfWeek = current.dayOfWeek,
                    startTime = current.startTime,
                    endTime = current.endTime,
                    date = current.date
                )
            )
        }

//        if (catalogResult.first.count() == 0) {
//            return null
//        } // happens when receiving one empty line
        return catalogResult
    }
}