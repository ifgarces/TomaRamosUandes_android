package com.ifgarces.tomaramosuandes.utils

import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import java.io.File
import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


/**
 * This will be an enhanced version for `CsvHandler`. Will replace it once it is completed and
 * tested.
 * @property CSV_FILE_NAME The file path of the asset CSV file with the offline catalog.
 * @property TIME_SEPARATOR Character that separates start and end times for an event.
 * @property DATE_FORMAT The format for CSV date values.
 * @property TIME_FORMAT The format for CSV time values.
 */
object CsvHandler2 {
    private const val CSV_FILE_NAME :String = "catalog_offline.csv"
    private const val TIME_SEPARATOR :String = "-"
    private val DATE_FORMAT :DateTimeFormatter = DateTimeFormatter.ofPattern(
        "dd/MM/yyyy", Locale("ES", "ES")
    )
    private val TIME_FORMAT :DateTimeFormatter = DateTimeFormatter.ISO_TIME

    /**
     * Maps each CSV column index to their corresponding content type.
     */
    private object CsvCols {
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
        //val SÁBADO        :Int = 14 //! won't be used by now, at least.
        val FECHA_INICIO  :Int = 15
        val FECHA_FIN     :Int = 16
        val SALA          :Int = 17
        val TIPO_EVENTO   :Int = 18
        val PROFESOR      :Int = 19

        fun toDayOfWeek(column :Int) :DayOfWeek? {
            return when (column) {
                this.LUNES -> DayOfWeek.MONDAY
                this.MARTES -> DayOfWeek.TUESDAY
                this.MIÉRCOLES -> DayOfWeek.WEDNESDAY
                this.JUEVES -> DayOfWeek.THURSDAY
                this.VIERNES -> DayOfWeek.FRIDAY
                //this.SÁBADO -> DayOfWeek.SATURDAY //! won't be used by now, at least
                else -> null
            }
        }
    }

    /**
     * Encapsulates values for identifying event types. Maps CSV bare values(s) for event type to its
     * actual value in the application model.
     */
    private val csvEventTypesMap = mapOf(
        "CLAS" to RamoEventType.CLAS,
        "AYUD" to RamoEventType.AYUD,
        "LABT" to RamoEventType.LABT,
        "TUTR" to RamoEventType.LABT, //* we will treat "tutoriales" as labs, for now (if they still exist...)
        "PRBA" to RamoEventType.PRBA,
        "EXAM" to RamoEventType.EXAM
    )

    /**
     * Modelling a row of the CSV file for further parsing.
     */
    private data class CsvRow(
        val pe            :String,
        val nrc           :Int,
        val conector_liga :String,
        val lista_cruzada :String,
        val materia       :String,
        val curso         :Int,
        val sección       :String, // could be an integer
        val nombre        :String,
        val crédito       :Int,
        val lunes         :Pair<LocalTime, LocalTime>?,
        val martes        :Pair<LocalTime, LocalTime>?,
        val miércoles     :Pair<LocalTime, LocalTime>?,
        val jueves        :Pair<LocalTime, LocalTime>?,
        val viernes       :Pair<LocalTime, LocalTime>?,
        //val sábado        :Pair<LocalTime, LocalTime>?,
        val fecha_inicio  :LocalDate,
        val fecha_fin     :LocalDate,
        val sala          :String,
        val tipo          :String,
        val profesor      :String
    ) {
        companion object {
            /**
             * E.g. for parsing values "11:30 - 13:20" to a pair of `LocalTime`s.
             */
            fun parseTimeInterval(colVal :String) :Pair<LocalTime, LocalTime>? {
                if (colVal.isBlank()) return null
                val buff :List<String> = colVal.split(TIME_SEPARATOR)
                return Pair(
                    LocalTime.parse(buff[0].trim(), TIME_FORMAT),
                    LocalTime.parse(buff[1].trim(), TIME_FORMAT)
                )
            }
            fun parseDate(colVal :String) :LocalDate {
                return LocalDate.parse(colVal, DATE_FORMAT)
            }
        }

        constructor(columns :List<String>) :this(
            pe = columns[CsvCols.PLAN_ESTUDIOS].trim(),
            nrc = columns[CsvCols.NRC].trim().toInt(),
            conector_liga = columns[CsvCols.CONECTOR_LIGA].trim(),
            lista_cruzada = columns[CsvCols.LISTA_CRUZADA].trim(),
            materia = columns[CsvCols.MATERIA].trim(),
            curso = columns[CsvCols.CURSONUM].trim().toInt(),
            sección = columns[CsvCols.SECCIÓN].trim(),
            nombre = columns[CsvCols.NOMBRE].trim(),
            crédito = columns[CsvCols.CRÉDITO].trim().toInt(),
            lunes = parseTimeInterval(columns[CsvCols.LUNES].trim()),
            martes = parseTimeInterval(columns[CsvCols.MARTES].trim()),
            miércoles = parseTimeInterval(columns[CsvCols.MIÉRCOLES].trim()),
            jueves = parseTimeInterval(columns[CsvCols.JUEVES].trim()),
            viernes = parseTimeInterval(columns[CsvCols.VIERNES].trim()),
            //sábado = parseTimeInterval(columns[CsvCols.SÁBADO].trim()),
            fecha_inicio = parseDate(columns[CsvCols.FECHA_INICIO].trim()),
            fecha_fin = parseDate(columns[CsvCols.FECHA_FIN].trim()),
            sala = columns[CsvCols.SALA].trim(),
            tipo = columns[CsvCols.TIPO_EVENTO].replace(Regex("[0-9]"), "").trim(), //! ignoring numbers, e.g. "PRBA 1" will be treated as "PRBA"
            profesor = columns[CsvCols.PROFESOR].trim(),
        )
    }

    public fun parseCsv(file :File) :Pair<List<Ramo>, List<RamoEvent>> {
        val fileStream :InputStream = file.inputStream()
        Logf.debug(this::class, "Parsing offline catalog CSV file...")
        val csvRows :List<CsvRow>
        try {
            val fileLines :MutableList<String> = fileStream.bufferedReader(Charsets.UTF_8)
                .readLines().toMutableList()
            fileLines.removeAt(0) // ignoring header row
            fileLines.removeIf { it.isBlank() } // removing blank lines
            csvRows = fileLines.map { CsvRow(it.split(",")) }
        } finally {
            fileStream.close()
        }

        val ramos :MutableList<Ramo> = mutableListOf()
        val events :MutableList<RamoEvent> = mutableListOf()

        csvRows.forEachIndexed { rowNum :Int, row :CsvRow ->
            // Checking if this row belongs to an already seen `Ramo`
            if (! ramos.map { it.NRC }.contains(row.nrc)) {
                ramos.add(
                    Ramo(
                        NRC = row.nrc,
                        nombre = row.nombre,
                        profesor = row.profesor,
                        créditos = row.crédito,
                        materia = row.materia,
                        curso = row.curso,
                        sección = row.sección,
                        planEstudios = row.pe,
                        conectLiga = row.conector_liga,
                        listaCruzada = row.lista_cruzada
                    )
                )
            }

            // We iterate over the values on the day ranges, i.e. building at least one `RamoEvent`
            // per CSV row. Also, iterating day columns per row along with the actual `DayOfWeeb`
            // object for propper parsing.
            listOf(
                Pair(DayOfWeek.MONDAY, row.lunes),
                Pair(DayOfWeek.TUESDAY, row.martes),
                Pair(DayOfWeek.WEDNESDAY, row.miércoles),
                Pair(DayOfWeek.THURSDAY, row.jueves),
                Pair(DayOfWeek.FRIDAY, row.viernes)
            ).forEach { (dayOfWeek :DayOfWeek, eventTimes :Pair<LocalTime, LocalTime>?) ->
                if (eventTimes != null) { // the event of this row exists on this day
                    try {
                        events.add(
                            RamoEvent(
                                ID = events.count(), // <- this was supossed to be automatic, but well...
                                ramoNRC = row.nrc,
                                type = csvEventTypesMap[row.tipo]!!,
                                location = row.sala,
                                dayOfWeek = dayOfWeek,
                                startTime = eventTimes.first,
                                endTime = eventTimes.second,
                                date = row.fecha_inicio
                            )
                        )
                    } catch (e :NullPointerException) {
                        // NullPointerException on invalid type value, which requires manually
                        // change typos in the file
                        Logf.error(this::class, "Error at line %d: unknown event type '%s'. Must be one of %s",
                            rowNum + 1, row.tipo, csvEventTypesMap.keys
                        )
                        throw e
                    }
                }
            }
        }

        return Pair(ramos, events)
    }
}