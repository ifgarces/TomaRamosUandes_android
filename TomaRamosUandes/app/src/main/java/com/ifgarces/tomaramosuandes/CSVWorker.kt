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
import java.util.Locale


/* Handles CSV parsing */
object CSVWorker {
    private val DATE_SEPARATOR :String = "/" // e.g. "09/10/2020"
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
    * On fatal parsing error (invalid `csv_contents`), prompts a dialog that informs it to the user
    * and terminates the app.
    */
    public fun parseCSV(activity :Activity, csv_contents :String) : List<Curso> {
        val catalogResult :MutableList<Curso> = mutableListOf()

        val NRCs :MutableList<Int> = mutableListOf()
        var line :List<String>
        val current = object {
            var NRC :Int = -1
            lateinit var dayOfWeek :DayOfWeek
            lateinit var eventType :String
            lateinit var startTime :LocalTime
            lateinit var endTime   :LocalTime
        }
        var stringAux :String
        var splitterAux :List<String>
        var eventAux :CursoEvent
        var eventsCounter :Int = 0


        for (lineNum :Int in (0..csv_contents.length-1)) {
            line = csv_contents.split(",")
            current.NRC = line[this.csv_columns.NRC].toInt()

            if (! NRCs.contains(current.NRC)) {
                NRCs.add(current.NRC)
                try {
                    catalogResult.add(
                        Curso(
                            NRC = current.NRC,
                            nombre = line[this.csv_columns.NOMBRE].trim(),
                            profesor = line[this.csv_columns.PROFESOR].trim(),
                            créditos = line[this.csv_columns.CRÉDITO].trim().toInt(),
                            materia = line[this.csv_columns.MATERIA].trim(),
                            cursoNum = line[this.csv_columns.CURSONUM].trim().toInt(),
                            secciónNum = line[this.csv_columns.SECCIÓN].trim().toInt(),
                            planEstudios = line[this.csv_columns.PLAN_ESTUDIOS].trim(),
                            connectLiga = line[this.csv_columns.CONECTOR_LIGA].trim(),
                            listaCruz = line[this.csv_columns.LISTA_CRUZADA].trim()
                        )
                    )
                }
                catch (e :NumberFormatException) { // `String.toInt()` error
                    Logf("[DataMaster] Fatal error: integer cast exception at CSV line %d (%s). Details: %s", lineNum, line, e)
                    this.parseErrorDialog(activity)
                }
            }

            // getting day of week and time interval of the event
            for ( column :Int in (this.csv_columns.LUNES .. this.csv_columns.VIERNES) ) {
                stringAux = line[column].replace(" ", "")
                if (stringAux != "") {
                    current.dayOfWeek = this.csv_columns.toDayOfWeek(column)!!
                    splitterAux = stringAux.split(this.TIME_SEPARATOR)
                    if (splitterAux.count() != 2) {
                        Logf("[DataMaster] Error: unexpected split result on row %d ('%s'). Could not get time interval for event.", lineNum, line)
                        this.parseErrorDialog(activity)
                    }

                    current.startTime = LocalTime.parse(splitterAux[0], this.time_format)
                    current.endTime = LocalTime.parse(splitterAux[1], this.time_format)
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

            when (current.eventType) {
                this.event_types.PRUEBA, this.event_types.EXAMEN -> {
                    eventAux.date = LocalDate.parse(line[this.csv_columns.FECHA_INICIO], DateTimeFormatter.ISO_DATE)
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
                    this.parseErrorDialog(activity)
                }
            }
        }

        return catalogResult
    }

    /* Shows a dialog informing of a fatal error when processing CSV data and terminates the hole program. */
    public fun parseErrorDialog(activity :Activity) {
        activity.infoDialog(
            title = "Error fatal",
            message = "No se pudo obtener el catálogo de ramos para este periodo. Intente más tarde.",
            onDismiss = { activity.finishAffinity() }
        )
    }
}