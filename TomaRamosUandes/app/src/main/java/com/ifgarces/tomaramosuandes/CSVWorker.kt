package com.ifgarces.tomaramosuandes

import android.app.Activity
import com.ifgarces.tomaramosuandes.models.Curso
import com.ifgarces.tomaramosuandes.models.CursoEvent
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.spanishUpperCase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


/* Handles CSV parsing */
object CSVWorker {
    private val EXPLICIT_COMMA_MARKER :String = ";;" // used to have elements like "element with;; comma" instead of "\"element with, comma\"" (harder to parse)

    private val TIME_SEPARATOR :String = "-" // e.g. "10:30 - 12:20"
    private val date_format = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("ES", "ES"))
    private val time_format = DateTimeFormatter.ISO_TIME

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
        val FECHA_FIN     :Int = 15 // <- EN DESUSO
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
        val PRUEBA      :String = "PRON"
        val EXAMEN      :String = "EXON"
        val LABORATORIO :String = "LBON"
        val AYUDANTÍA   :String = "AYON"
        val TUTORIAL    :String = "TUTR"
        val CLASE       :String = "OLIN"
    }


    /**
    * Converts the contents of the CSV (holding the period catalog) into a collection of `Curso`.
    * On fatal parsing error (invalid `csv_contents`), returns null.
    */
    public fun parseCSV(csv_lines :List<String>) : List<Curso>? {
        val catalogResult :MutableList<Curso> = mutableListOf()

        val NRCs :MutableList<Int> = mutableListOf()
        var line :List<String>
        val current = object {
            var NRC :Int = 0
            var cursoNum :Int = 0
            lateinit var dayOfWeek :DayOfWeek
            lateinit var eventType :String
            lateinit var startTime :LocalTime
            lateinit var endTime   :LocalTime
        }
        var stringAux :String
        var splitterAux :List<String>
        var eventAux :CursoEvent
        var eventsCounter :Int = 0
        var lineNum :Int = 0
        var badNRCsCount :Int = 0

        while (lineNum++ < csv_lines.count()-1) {
            line = csv_lines[lineNum].split(",")

            try {
                current.NRC = line[this.csv_columns.NRC].toInt()
            }
            catch (e :NumberFormatException) {
                Logf("[CSVWorker] Warning: NRC could not be parsed to integer at CSV line %d (%s). Details: %s. Assuming NRC assign is yet pending and assigning negative one.", lineNum, csv_lines[lineNum], e)
                current.NRC = -(badNRCsCount++)
            }

            try {
                current.cursoNum = line[this.csv_columns.CURSONUM].toInt()
            }
            catch (e :NumberFormatException) {
                Logf("[CSVWorker] Warning: CursoNum could not be parsed to integer at CSV line %d (%s). Details: %s. Assuming CursoNum assign is yet pending and assigning zero.", lineNum, csv_lines[lineNum], e)
                current.cursoNum = 0
            }


            if (! NRCs.contains(current.NRC)) {
                NRCs.add(current.NRC)
                try {
                    catalogResult.add(
                        Curso(
                            NRC = current.NRC,
                            nombre = line[this.csv_columns.NOMBRE].trim(),
                            profesor = line[this.csv_columns.PROFESOR].trim(),
                            créditos = line[this.csv_columns.CRÉDITO].toInt(),
                            materia = line[this.csv_columns.MATERIA].trim().replace(this.EXPLICIT_COMMA_MARKER, ","),
                            cursoNum = current.cursoNum,
                            secciónNum = line[this.csv_columns.SECCIÓN].trim().replace(this.EXPLICIT_COMMA_MARKER, ","),
                            planEstudios = line[this.csv_columns.PLAN_ESTUDIOS].trim().replace(this.EXPLICIT_COMMA_MARKER, ","),
                            connectLiga = line[this.csv_columns.CONECTOR_LIGA].trim().replace(this.EXPLICIT_COMMA_MARKER, ","),
                            listaCruz = line[this.csv_columns.LISTA_CRUZADA].trim().replace(this.EXPLICIT_COMMA_MARKER, ",")
                        )
                    )
                }
                catch (e :NumberFormatException) { // `String.toInt()` error
                    Logf("[CSVWorker] Error: integer cast exception at CSV line %d (%s). Details: %s", lineNum, csv_lines[lineNum], e)
                    return null
                }

            }

            // getting day of week and time interval of the event
            for ( column :Int in (this.csv_columns.LUNES .. this.csv_columns.VIERNES) ) {
                stringAux = line[column].replace(" ", "")
                if (stringAux != "") {
                    current.dayOfWeek = this.csv_columns.toDayOfWeek(column)!!
                    splitterAux = stringAux.split(this.TIME_SEPARATOR)
                    if (splitterAux.count() != 2) {
                        Logf("[CSVWorker] Error: unexpected split result on row %d ('%s'). Could not get time interval for event.", lineNum, line)
                        return null
                    }

                    try {
                        current.startTime = LocalTime.parse(splitterAux[0], this.time_format)
                        current.endTime = LocalTime.parse(splitterAux[1], this.time_format)
                    }
                    catch (e :DateTimeParseException) {
                        Logf("[CSVWorker] Error: Time parsing exception at CSV line %d (%s). Details: %s", lineNum, csv_lines[lineNum], e)
                        return null
                    }
                    break
                }
            }

            current.eventType = line[this.csv_columns.TIPO_EVENTO].spanishUpperCase().replace(" ", "")
            eventAux = CursoEvent(
                ID = eventsCounter++,
                cursoNRC = current.NRC,
                day = current.dayOfWeek,
                startTime = current.startTime,
                endTime = current.endTime
            )

            when ( current.eventType.replace(regex=Regex("[0-9 ]"), replacement="") ) { // "PRON1" -> "PRON", etc.
                this.event_types.PRUEBA, this.event_types.EXAMEN -> {
                    try {
                        eventAux.date = LocalDate.parse(line[this.csv_columns.FECHA_INICIO], this.date_format)
                    }
                    catch (e :DateTimeParseException) {
                        Logf("[CSVWorker] Error: Date parsing exception at CSV line %d (%s). Details: %s", lineNum, csv_lines[lineNum], e)
                        return null
                    }
                    catalogResult.last().evaluaciones.add( eventAux )
                }
                this.event_types.CLASE -> {
                    catalogResult.last().clases.add( eventAux )
                }
                this.event_types.LABORATORIO -> {
                    catalogResult.last().laboratorios.add( eventAux )
                }
                this.event_types.AYUDANTÍA, this.event_types.TUTORIAL -> {
                    catalogResult.last().ayudantías.add( eventAux )
                }
                else -> {
                    Logf("[CSVWorker] Error at CSV line %d ('%s'): unknown event type '%s'", lineNum, line, current.eventType)
                    return null
                }
            }
        }

        return catalogResult
    }

    /* Shows a dialog informing of a fatal error when processing CSV data and terminates the hole program. */
    public fun fatalErrorDialog(activity :Activity) {
        Logf("[CSVWorker] Launching fatal error dialog...")
        activity.runOnUiThread {
            activity.infoDialog(
                title = "Error fatal",
                message = "No se pudo obtener el catálogo de ramos para este periodo. Intente más tarde.",
                onDismiss = { activity.finishAffinity() }
            )
        }
    }
}