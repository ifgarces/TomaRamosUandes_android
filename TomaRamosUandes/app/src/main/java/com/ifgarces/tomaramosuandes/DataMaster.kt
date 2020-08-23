package com.ifgarces.tomaramosuandes

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.time.DayOfWeek
import java.util.concurrent.locks.ReentrantLock


/**
 * Handles the database.
 * @property catalog Contains the collection of `Ramo` available for the current period.
 */
object DataMaster {

    // ----
    // TODO: make sure to manage write concurrency for `userRamos`
    // ----

    @Volatile private lateinit var catalog   :List<Ramo>;        fun getCatalog() = this.catalog
    @Volatile private lateinit var userRamos :MutableList<Ramo>; fun getUserRamos() = this.userRamos
    private lateinit var writeLock :ReentrantLock // concurrency write lock for `userRamos`

    /**
     * Fetches the `catalog` from a the internet, calling `WebManager`.
     * @param clearDatabase If true, deletes the local Room database.
     * @param onSuccess Executed when successfully finished database initialization.
     * @param onInternetError Executed when the data file can't be fetched or its elements are invalid somehow.
     * @param onRoomError Executed when it is not possible to load user's local Room database.
     */
    fun init(
        clearDatabase   :Boolean,
        onSuccess       :() -> Unit,
        onInternetError :() -> Unit,
        onRoomError     :() -> Unit
    ) {
        this.catalog = listOf()
        this.userRamos = mutableListOf()
        this.writeLock = ReentrantLock()
        AsyncTask.execute {
            try {
                Logf("[DataMaster] Fetching CSV catalog data...")
                val csv_body :String = WebManager.fetchCatalogCSV()

                Logf("[DataMaster] Parsing CSV...")
                this.catalog = CSVWorker.parseCSV(csv_lines=csv_body.split("\n"))!!
                Logf("[DataMaster] CSV parsing complete. Catalog size: %d", this.catalog.count())

                // TODO: load user Room DB

                onSuccess.invoke()
            }
            catch (e :java.net.UnknownHostException) {
                Logf("[DataMaster] Could not load online CSV (internet connection error)")
                onInternetError.invoke()
            }
            catch (e :NullPointerException) {
                Logf("[DataMaster] Invalid online CSV data.")
                onInternetError.invoke()
            }
        }
    }

    /**
     * Searchs and returns the `Ramo` whose NRC (ID) matches.
     * @param NRC The fiven ID.
     * @param searchInUserList If true, will search just along the user taken `Ramo`s. If not, searches along the catalog.
     * @return Returns null if not found.
     */
    public fun findRamo(NRC :Int, searchInUserList :Boolean = false) : Ramo? {
        this.catalog.forEach {
            if (it.NRC == NRC) { return it }
        }
        return null
    }

    /**
     * Attemps to take `ramo` and add it to the user list. If any event of `ramo` collides with
     * another already taken `Ramo`, prompts confirmation dialog and, if the user wants to continue
     * neverdeless, finishes the action and returns true. If not, returns false.
     */
    public fun takeRamo(ramo :Ramo, context :Context, onClose :() -> Unit) {
        var conflictReport :String = ""

        ramo.events.forEach {
            this.getConflictsOf(it).forEach { conflictedEvent :RamoEvent ->
                conflictReport += "• %s\n".format(conflictedEvent.toShortString())
            }
        }

        if (conflictReport == "") { // no conflict was found
            takeRamoAction(ramo)
            onClose.invoke()
        } else { // conflict(s) found
            context.yesNoDialog(
                title = "Conflicto de eventos",
                message = "Advertencia, los siguientes eventos entran en conflicto:\n\n%s\n¿Tomar %s de todas formas?"
                    .format(conflictReport, ramo.nombre),
                onYesClicked = {
                    takeRamoAction(ramo)
                    onClose.invoke()
                },
                onNoClicked = {
                    onClose.invoke()
                },
                icon = R.drawable.alert_icon
            )
        }
    }
    private fun takeRamoAction(ramo :Ramo) {
        try {
            this.writeLock.lock()
            this.userRamos.add(ramo)
            // TODO: update Room DB
        } finally {
            this.writeLock.unlock()
        }
    }

    public fun eraseRamo(NRC :Int) {
        var index :Int = 0
        while (index < this.userRamos.count()) {
            if (this.userRamos[index].NRC == NRC) {
                try {
                    this.writeLock.lock()
                    this.userRamos.removeAt(index)
                    // TODO: update Room DB
                } finally {
                    this.writeLock.unlock()
                }
            }
            index++
        }
    }

    public fun getUserTotalCredits() : Int {
        var creditosTotal :Int = 0
        this.userRamos.forEach {
            creditosTotal += it.créditos
        }
        return creditosTotal
    }

    /**
     * Checks if the two events are in conflict. Returns true if there exist an overlaping/collision
     * between them. Will return null if invalid data is passed (error), such as if trying to pass
     * an evaluation event with a non-evaluation event, or if their `date` is null (when
     * passing evaluation evaluation).
     */
    public fun areEventsConflicted(ev1 :RamoEvent, ev2 :RamoEvent) : Boolean? {
        val first_isEval :Boolean = ev1.isEvaluation()
        val second_isEval :Boolean = ev2.isEvaluation()
        if (first_isEval && second_isEval) { // comparing evaluation events (date and time)
            if (ev1.date == null || ev2.date == null) { return null } // error: missing date
            if (ev1.date != ev2.date) { return false }
        }
        else if ( (!first_isEval) && (!second_isEval) ) { // comparing non-evaluation events (dayOfWeek and time)
            if (ev1.dayOfWeek != ev2.dayOfWeek) { return false }
        }
        else { return null } // unconsistent types: comparing evaluation with non-evaluation

        // "does i starts before j finished and i finishes after j starts?"
        return (ev1.startTime <= ev2.endTime) && (ev1.endTime >= ev2.startTime)
    }


    /**
     * Iterates all user taken `Ramo` list and checks if `event` collide with another.
     * @return The other events that collide with `event`, including itself (first).
     * The list will be empty if there is no conflict.
     */
    public fun getConflictsOf(event :RamoEvent) : List<RamoEvent> {
        val conflicts :MutableList<RamoEvent> = mutableListOf()
        this.userRamos.forEach {
            it.events.forEach { ev :RamoEvent ->
                if (ev.ID != event.ID) {
                    if (this.areEventsConflicted(ev, event) == true) { conflicts.add(ev) }
                }
            }
        }
        if (conflicts.count() > 0) { conflicts.add(index=0, element=event) } // addint itself, in order to inform to user in conflict dialog
        return conflicts
    }

    /* Gets all the non-evaluation events, filtered by each non-weekend `DayOfWeek` */
    public fun getEventsByWeekDay(ramos :List<Ramo> = this.userRamos) : Map<DayOfWeek, List<RamoEvent>> {
        val results :MutableMap<DayOfWeek, MutableList<RamoEvent>> = mutableMapOf(
            DayOfWeek.MONDAY to mutableListOf(),
            DayOfWeek.TUESDAY to mutableListOf(),
            DayOfWeek.WEDNESDAY to mutableListOf(),
            DayOfWeek.THURSDAY to mutableListOf(),
            DayOfWeek.FRIDAY to mutableListOf()
        )
        for (ramo :Ramo in ramos) {
            for (event :RamoEvent in ramo.events) {
                if (! event.isEvaluation()) {
                    when(event.dayOfWeek) {
                        DayOfWeek.MONDAY    -> { results[DayOfWeek.MONDAY]?.add(event) }
                        DayOfWeek.TUESDAY   -> { results[DayOfWeek.TUESDAY]?.add(event) }
                        DayOfWeek.WEDNESDAY -> { results[DayOfWeek.WEDNESDAY]?.add(event) }
                        DayOfWeek.THURSDAY  -> { results[DayOfWeek.THURSDAY]?.add(event) }
                        DayOfWeek.FRIDAY    -> { results[DayOfWeek.FRIDAY]?.add(event) }
                        else -> {} // ignored
                    }
                }
            }
        }
        return results
    }

    /* Creates the iCalendar (ICS) file for the tests and exams for all courses in `ramos`, storing it at `savePath` */
    public fun exportICS(ramos :List<Ramo> = this.userRamos, context :Context) {
        val saveFolder :String = "Temp"
        val fileContent :String = this.buildIcsContent()

        fun writeFileToStream(content :String, stream :OutputStream) {
            try {
                stream.write(content.toByteArray())
                stream.close()
                Logf("[DataMaster] ICS file successfully exported inside folder '%s'.", saveFolder)
            }
            catch (e :IOException) {
                Logf("[DataMaster] Failed to export ICS file. %s", e)
            }
        }

        val fileMetadata :ContentValues = ContentValues()
        fileMetadata.put(MediaStore.Images.Media.MIME_TYPE, "text/calendar")
        fileMetadata.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            fileMetadata.put(MediaStore.Images.Media.RELATIVE_PATH, saveFolder)
            fileMetadata.put(MediaStore.Images.Media.IS_PENDING, true)

            val uri :Uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileMetadata)!!
            writeFileToStream(content=fileContent, stream=context.contentResolver.openOutputStream(uri)!!)
            fileMetadata.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, fileMetadata, null, null)
        }
        else {
            val directory = File( "%s/%s".format(Environment.getExternalStorageDirectory().toString(), saveFolder) )
            if (! directory.exists()) { directory.mkdirs() }

            val fileName :String = "%s.ics".format(System.currentTimeMillis().toString())
            val file = File(directory, fileName)
            writeFileToStream(content=fileContent, stream=FileOutputStream(file))
            fileMetadata.put(MediaStore.Images.Media.DATA, file.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileMetadata)
        }
    }

    /* Generates the ICS file contents and returns it */
    private fun buildIcsContent(data :List<Ramo> = this.userRamos) : String {
        val fileHeader :String = """BEGIN:VCALENDAR
PRODID:-//Google Inc//Google Calendar 70.9054//EN
VERSION:2.0
CALSCALE:GREGORIAN
METHOD:PUBLISH
X-WR-CALNAME:CalendarioEvaluaciones
X-WR-TIMEZONE:America/Santiago"""
        var fileBody :String = ""
        val fileFooter :String = "END:VCALENDAR"

        var year      :String
        var month     :String
        var day       :String
        var startTime :String
        var endTime   :String

        /* processing evaluations... */
        data.forEach { ramo :Ramo ->
            ramo.events.forEach {
                if (it.isEvaluation()) {
                    year = it.date!!.year.toString()
                    month = it.date!!.month.toString()
                    day = it.date!!.dayOfMonth.toString()
                    startTime = it.startTime.toString().replace(":", "")
                    endTime = it.endTime.toString().replace(":", "")

                    fileBody += """
BEGIN:VEVENT
DTSTART;TZID=America/Santiago:${year}${month}${day}T${startTime}00
DTEND;TZID=America/Santiago:${year}${month}${day}T${endTime}00
DESCRIPTION:[${ramo.materia} ${ramo.NRC}] ${ramo.nombre}
STATUS:CONFIRMED
SUMMARY:${SpanishStringer.ramoEventType(eventType=it.type, shorten=false)} ${ramo.nombre} (${it.startTime})
END:VEVENT
"""
                }
            }
        }

        return "%s%s%s".format(fileHeader, fileBody, fileFooter)
    }
}