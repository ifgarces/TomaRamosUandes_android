package com.ifgarces.tomaramosuandes.utils

import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Handles CSV parsing, for the case in which there is no internet connection (or interaction with
 * Firebase fails) and the offline catalog included in the APK as a CSV file must be loaded. The
 * mentioned file is the equivalent version of the Excel file exposed by the Engineering school on
 * Canvas (previously in SAF) with bare information about courses for the next academic period.
 * @property TIME_SEPARATOR The symbol used to separate a beggining time from an ending time for an
 * event in the CSV, e.g. "10:30 - 12:20".
 * @property CSV_FILE_NAME The file path of the asset CSV file with the offline catalog.
 * @property TIME_SEPARATOR Character that separates start and end times for an event.
 * @property DATE_FORMAT The format for CSV date values.
 * @property TIME_FORMAT The format for CSV time values.
 * @property EXPLICIT_COMMA_CHAR For adding a comma to a `Ramo.nombre` attribute. As CSV parsing is
 * simply splitting "," characters, this is needed.
 */
object CsvHandler {
    public const val CSV_FILE_NAME :String = "catalog_offline.csv"
    private const val TIME_SEPARATOR :String = "-"
    private val DATE_FORMAT :DateTimeFormatter = DateTimeFormatter.ofPattern(
        "dd/MM/yyyy", Locale("ES", "ES")
    )
    private val TIME_FORMAT :DateTimeFormatter = DateTimeFormatter.ISO_TIME
    private val EXPLICIT_COMMA_CHAR :Char = ";".single()

    /**
     * Auxiliar method for transforming date with format e.g. "d/M/yyyy" to the right "dd/MM/yyyy".
     * Otherwise, date parsing will fail, as it would not perfectly match `CsvHandler.DATE_FORMAT`.
     */
    private fun preProcessTimeFormat(date :String) :String {
        var buff :String = ""
        date.split("/").forEach { digits :String ->
            if (digits.count() == 1) buff += "0%s/".format(digits) // prepending zero when needed
            else buff += "%s/".format(digits)
        }
        return buff.dropLast(1) // removing extra trailing '/' character
    }

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
    }

    /**
     * Encapsulates values for identifying event types. Maps CSV bare values(s) for event type to
     * its actual value in the application model.
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
     * Modeling a row of the CSV file for further parsing.
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
        val fecha_inicio  :LocalDate?,
        val fecha_fin     :LocalDate?,
        val sala          :String,
        val tipo          :String,
        val profesor      :String
    ) {
        companion object {
            /**
             * E.g. for parsing string values "11:30 - 13:20" to a pair of `LocalTime` boundaries.
             * Will return null when the passed `colVal` is an empty string (non-filled CSV cell).
             */
            fun parseTimeInterval(cellValue :String) :Pair<LocalTime, LocalTime>? {
                if (cellValue.isBlank()) return null
                val buff :List<String> = cellValue.split(TIME_SEPARATOR)
                return Pair(
                    LocalTime.parse(buff[0].trim(), TIME_FORMAT),
                    LocalTime.parse(buff[1].trim(), TIME_FORMAT)
                )
            }

            /**
             * For parsing a given date. Returns null if the string passed is blank.
             */
            fun parseDate(cellValue :String) :LocalDate? {
                if (cellValue.isBlank()) return null // this seems to happen always for PEG courses, and may occur for other courses as well
                return LocalDate.parse(preProcessTimeFormat(cellValue), DATE_FORMAT)
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
            nombre = columns[CsvCols.NOMBRE].trim().replace(EXPLICIT_COMMA_CHAR, ','),
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

    /**
     * Parses the given CSV file and returns a pair with the list of `Ramo`s and their `RamoEvent`s.
     * @param fileStream Used this way (instead of just using `CsvHandler.CSV_FILE_NAME` in order to
     * implement proper tests. The file must be the asset file at that mentioned path.
     */
    public fun parseCsv(fileStream :InputStream) :Pair<List<Ramo>, List<RamoEvent>> {
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
            // Checking if this row belongs to an already seen `Ramo`, and creating if not
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