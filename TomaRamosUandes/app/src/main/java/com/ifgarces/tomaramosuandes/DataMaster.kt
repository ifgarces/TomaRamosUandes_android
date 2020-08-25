package com.ifgarces.tomaramosuandes

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import androidx.room.Room
import com.ifgarces.tomaramosuandes.local_db.LocalRoomDB
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
 * @property catalog_ramos Contains the collection of `Ramo` available for the current period.
 */
object DataMaster {

    // ----
    // TODO: make sure to manage write concurrency for `userRamos`
    // ----

    private lateinit var localDB :LocalRoomDB

    @Volatile private lateinit var catalog_ramos  :List<Ramo>;      fun getCatalogRamos() = this.catalog_ramos
    @Volatile private lateinit var catalog_events :List<RamoEvent>; fun getCatalogEvents() = this.catalog_events

    @Volatile private lateinit var user_ramos  :MutableList<Ramo>;       fun getUserRamos() = this.user_ramos
    @Volatile private lateinit var user_events :MutableList<RamoEvent>; fun getUserEvents() = this.user_events

    private lateinit var writeLock :ReentrantLock // concurrency write lock for `userRamos`

    /**
     * Fetches the `catalog` from a the internet, calling `WebManager`.
     * @param clearDatabase If true, deletes the local Room database.
     * @param onSuccess Executed when successfully finished database initialization.
     * @param onInternetError Executed when the data file can't be fetched or its elements are invalid somehow.
     * @param onRoomError Executed when it is not possible to load user's local Room database.
     */
    fun init(
        activity        :Activity,
        clearDatabase   :Boolean,
        onSuccess       :() -> Unit,
        onInternetError :() -> Unit,
        onRoomError     :() -> Unit
    ) {
        this.catalog_ramos = listOf()
        this.user_ramos = mutableListOf()
        this.user_events = mutableListOf()
        this.writeLock = ReentrantLock()

        AsyncTask.execute {
            try {
                this.localDB = Room.databaseBuilder(activity, LocalRoomDB::class.java, Ramo.TABLE_NAME).build()
            }
            catch (e :Exception) {
                Logf("[DataMaster] Error: could not load local room database. %s", e)
                onRoomError.invoke()
            }

            try {
                Logf("[DataMaster] Fetching CSV catalog data...")
                val csv_body :String = WebManager.fetchCatalogCSV()

                Logf("[DataMaster] Parsing CSV...")
                val aux :Pair<List<Ramo>, List<RamoEvent>> = CSVWorker.parseCSV(csv_lines=csv_body.split("\n"))!!
                this.catalog_ramos = aux.first
                this.catalog_events = aux.second
                // TODO: manage excption inside last function from invalid data (no longer working on this verison). This indicates that the app is very outdated and can't get the catalog.

                Logf("[DataMaster] CSV parsing complete. Catalog size: %d", this.catalog_ramos.count())

                if (clearDatabase) { // removing all data in memory and in local room database
                    this.user_ramos.clear()
                    this.user_events.clear()
                    this.localDB.ramosDAO().clear()
                    this.localDB.eventsDAO().clear()
                    Logf("[DataMaster] Local database cleaned.")
                }

                this.user_ramos = this.localDB.ramosDAO().getAllRamos().toMutableList()
                this.user_events = this.localDB.eventsDAO().getAllEvents().toMutableList()
                Logf("[DataMaster] Recovered user local data: %s ramos (with %d events).", this.user_ramos.count(), this.user_events.count())
                activity.runOnUiThread {
                    if (this.user_ramos.count() > 0) {
                        activity.toastf("Se recuperó su conjunto de ramos tomados.")
                    }
                }

                onSuccess.invoke()
            }
            catch (e :java.net.UnknownHostException) {
                Logf("[DataMaster] Could not load online CSV (internet connection error)")
                onInternetError.invoke()
            }
            catch (e :NullPointerException) {
                Logf("[DataMaster] Invalid online CSV data (for this app version).")
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
        this.catalog_ramos.forEach {
            if (it.NRC == NRC) { return it }
        }
        return null
    }

    /**
     * Attemps to take `ramo` and add it to the user list. If any event of `ramo` collides with
     * another already taken `Ramo`, prompts confirmation dialog and, if the user wants to continue
     * neverdeless, finishes the action and returns true. If not, returns false.
     */
    public fun takeRamo(ramo :Ramo, activity :Activity, onClose :() -> Unit) {
        var conflictReport :String = ""

        AsyncTask.execute {
            this.catalog_events.filter { it.ramoNRC==ramo.NRC }
                .forEach { event :RamoEvent ->
                    this.getConflictsOf(event).forEach { conflictedEvent :RamoEvent ->
                        conflictReport += "• %s\n".format(conflictedEvent.toShortString())
                    }
            }

            if (conflictReport == "") { // no conflict was found
                this.takeRamoAction(ramo)
                onClose.invoke()
            } else { // conflict(s) found
                activity.runOnUiThread {
                    activity.yesNoDialog(
                        title = "Conflicto de eventos",
                        message = "Advertencia, los siguientes eventos entran en conflicto:\n\n%s\n¿Tomar %s de todas formas?"
                            .format(conflictReport, ramo.nombre),
                        onYesClicked = {
                            this.takeRamoAction(ramo)
                            onClose.invoke()
                        },
                        onNoClicked = {
                            onClose.invoke()
                        },
                        icon = R.drawable.alert_icon
                    )
                }
            }
        }
    }
    private fun takeRamoAction(ramo :Ramo) {
        try {
            this.writeLock.lock()
            this.user_ramos.add(ramo)
            this.localDB.ramosDAO().insert(ramo) // assuming we're already in a separate thread
            this.catalog_events.filter { it.ramoNRC == ramo.NRC }
                .forEach { event :RamoEvent ->
                    this.localDB.eventsDAO().insert(event)
                }
            Logf("[DataMaster] Ramo{NRC=%s} taken.", ramo.NRC)
        } finally {
            this.writeLock.unlock()
        }
    }

    /* Removes the matching `Ramo` from the user taken list. */
    public fun untakeRamo(NRC :Int) {
        var index :Int = 0
        while (index < this.user_ramos.count()) {
            if (this.user_ramos[index].NRC == NRC) {
                try {
                    this.writeLock.lock()
                    this.user_ramos.removeAt(index)
                    AsyncTask.execute { this.localDB.ramosDAO().deleteRamo(nrc=NRC) }
                } finally {
                    this.writeLock.unlock()
                }
            }
            index++
        }
    }

    /* Gets the total amount of `crédito` for the user taken list. */
    public fun getUserCreditSum() : Int {
        var creditosTotal :Int = 0
        this.user_ramos.forEach {
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
     * [This function needs to be called on a separated thread]
     * Iterates all user taken `Ramo` list and checks if `event` collide with another.
     * @return The other events that collide with `event`, including itself (first).
     * The list will be empty if there is no conflict.
     */
    public fun getConflictsOf(event :RamoEvent) : List<RamoEvent> {
        val conflicts :MutableList<RamoEvent> = mutableListOf()
        this.user_ramos.forEach {
            this.localDB.eventsDAO().getEventsOfRamo(nrc=it.NRC).forEach { ev :RamoEvent ->
                if (ev.ID != event.ID) {
                    if (this.areEventsConflicted(ev, event) == true) { conflicts.add(ev) }
                }
            }
        }
        if (conflicts.count() > 0) { conflicts.add(index=0, element=event) } // addint itself, in order to inform to user in conflict dialog
        return conflicts
    }

    /**
     * [This function needs to be called on a separated thread]
     * Gets all the non-evaluation events, filtered by each non-weekend `DayOfWeek`
     */
    public fun getEventsByWeekDay(ramos :List<Ramo> = this.user_ramos) : Map<DayOfWeek, List<RamoEvent>> {
        val results :MutableMap<DayOfWeek, MutableList<RamoEvent>> = mutableMapOf(
            DayOfWeek.MONDAY to mutableListOf(),
            DayOfWeek.TUESDAY to mutableListOf(),
            DayOfWeek.WEDNESDAY to mutableListOf(),
            DayOfWeek.THURSDAY to mutableListOf(),
            DayOfWeek.FRIDAY to mutableListOf()
        )
        var events :List<RamoEvent>
        ramos.forEach { ramo :Ramo ->
            events = this.localDB.eventsDAO().getEventsOfRamo(nrc=ramo.NRC)
            events.forEach { event :RamoEvent ->
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

    /**
     * [This function needs to be called on a separated thread]
     * Creates the iCalendar (ICS) file for the tests and exams for all courses in `ramos`, storing it at `savePath`
     */
    public fun exportICS(ramos :List<Ramo> = this.user_ramos, context :Context) {
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

    /**
     * [This function needs to be called on a separated thread]
     * Generates the ICS file contents and returns it.
     */
    private fun buildIcsContent(ramos :List<Ramo> = this.user_ramos) : String {
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
        ramos.forEach { ramo :Ramo ->
            this.localDB.eventsDAO().getEventsOfRamo(nrc=ramo.NRC).forEach { event :RamoEvent ->
                if (event.isEvaluation()) {
                    year = event.date!!.year.toString()
                    month = event.date!!.month.toString()
                    day = event.date!!.dayOfMonth.toString()
                    startTime = event.startTime.toString().replace(":", "")
                    endTime = event.endTime.toString().replace(":", "")

                    fileBody += """
BEGIN:VEVENT
DTSTART;TZID=America/Santiago:${year}${month}${day}T${startTime}00
DTEND;TZID=America/Santiago:${year}${month}${day}T${endTime}00
DESCRIPTION:[${ramo.materia} ${ramo.NRC}] ${ramo.nombre}
STATUS:CONFIRMED
SUMMARY:${SpanishStringer.ramoEventType(eventType=event.type, shorten=false)} ${ramo.nombre} (${event.startTime})
END:VEVENT
"""
                }
            }
        }

        return "%s%s%s".format(fileHeader, fileBody, fileFooter)
    }
}