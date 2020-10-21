package com.ifgarces.tomaramosuandes

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.room.Room
import com.ifgarces.tomaramosuandes.local_db.LocalRoomDB
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.UserStats
import com.ifgarces.tomaramosuandes.utils.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.time.DayOfWeek
import java.util.concurrent.locks.ReentrantLock


/**
 * Handles the database.
 * @property catalog_ramos Contains the collection of available `Ramo` for the current period.
 * @property catalog_events Collection of available `RamoEvents`.
 * @property user_ramos Set of inscribed `Ramo` by the user.
 * @property user_events Set of inscribed `RamoEvent` by the user.
 * @property writeLock Concurrency write lock for `user_ramos`.
 */
object DataMaster {

    // ----
    // TODO: make 100% sure to manage write concurrency for `userRamos` variable
    // ----

    private lateinit var localDB :LocalRoomDB

    @Volatile private lateinit var catalog_ramos  :List<Ramo>;      fun getCatalogRamos() = this.catalog_ramos
    @Volatile private lateinit var catalog_events :List<RamoEvent>; fun getCatalogEvents() = this.catalog_events
    private lateinit var user_stats               :UserStats;       fun getUserStats() = this.user_stats

    @Volatile private lateinit var user_ramos  :MutableList<Ramo>;      fun getUserRamos() = this.user_ramos
    @Volatile private lateinit var user_events :MutableList<RamoEvent>; fun getUserEvents() = this.user_events

    private lateinit var writeLock :ReentrantLock

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
                    this.clean()
                }

                this.user_ramos = this.localDB.ramosDAO().getAllRamos().toMutableList()
                this.user_events = this.localDB.eventsDAO().getAllEvents().toMutableList()
                with (this.localDB.userStatsDAO().getStats()) {
                    if (this.count() == 0) { // happens at first run of the app since installation
                        // TODO: check what happens on upgrade, i.e. when installing a newer version with the app already installed
                        this@DataMaster.user_stats = UserStats()
                        this@DataMaster.user_stats.firstRunOfApp = false
                        this@DataMaster.localDB.userStatsDAO().insert(this@DataMaster.user_stats)
                        this@DataMaster.user_stats.firstRunOfApp = true
                    } else {
                        this@DataMaster.user_stats = this.first()
                        //this@DataMaster.user_stats.firstRunOfApp = false // <- this line should not be necessary if the stats are saved to Room database always on app close.
                    }
                }
                Logf("[DataMaster] Recovered user local data: %s ramos (with %d events).", this.user_ramos.count(), this.user_events.count())

                if (this.user_ramos.count() > 0) {
                    activity.runOnUiThread { activity.toastf("Se recuperó su conjunto de ramos tomados.") }
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

    private fun clean() {
        this.user_ramos.clear()
        this.user_events.clear()
        this.localDB.ramosDAO().clear()
        this.localDB.eventsDAO().clear()
        this.localDB.userStatsDAO().clear()
        Logf("[DataMaster] Local database cleaned.")
    }

    public fun getRamoEventsOf(ramo :Ramo) : List<RamoEvent> {
        return this.user_events.filter{ it.ramoNRC == ramo.NRC }
    }

    /**
     * Will be called when the joke dialog of `EasterEggs` is executed. It is a way to inform it to
     * `DataMaster` and its Room local database.
     */
    public fun jokeDialogWasExecuted() {
        AsyncTask.execute {
            this.user_stats.jokeExecuted = true
            this.localDB.userStatsDAO().update(this.user_stats)
        }
    }

    /**
     * Searchs and returns the `Ramo` whose NRC (ID) matches.
     * @param NRC The fiven ID.
     * @param searchInUserList If true, will search just along the user inscribed `Ramo`s. If not,
     * searches along the catalog.
     * @return Returns null if not found.
     */
    public fun findRamo(NRC :Int, searchInUserList :Boolean = false) : Ramo? {
        if (searchInUserList) {
            this.user_ramos.forEach {
                if (it.NRC == NRC) { return it }
            }
        }
        else {
            this.catalog_ramos.forEach {
                if (it.NRC == NRC) { return it }
            }
        }
        return null
    }

    /**
     * Attemps to inscribe `ramo` and add it to the user list. If any event of `ramo` collides with
     * another already inscribed `Ramo`, prompts conflict confirmation dialog and, if the user
     * decides to continue neverdeless, finishes the action.
     * @param onFinish Will be executed when the inscription operation finished (because this is asyncronous).
     */
    public fun inscribeRamo(ramo :Ramo, activity :Activity, onFinish :() -> Unit) {
        var conflictReport :String = ""

        AsyncTask.execute {  // the hole function must be async because of this.getConflictsOf()
            this.catalog_events.filter{ it.ramoNRC==ramo.NRC }
                .forEach { event :RamoEvent ->
                    this.getConflictsOf(event).forEach { conflictedEvent :RamoEvent ->
                        conflictReport += "• %s\n".format(conflictedEvent.toShortString())
                    }
                }

            if (conflictReport == "") { // no conflict was found
                AsyncTask.execute {
                    this.inscribeRamoAction(ramo)
                    onFinish.invoke()
                }
            } else { // conflict(s) found
                activity.runOnUiThread {
                    activity.yesNoDialog(
                        title = "Conflicto de eventos",
                        message = "Advertencia, los siguientes eventos entran en conflicto:\n\n%s\n¿Tomar %s de todas formas?"
                            .format(conflictReport, ramo.nombre),
                        onYesClicked = {
                            AsyncTask.execute {
                                this.inscribeRamoAction(ramo)
                                onFinish.invoke()
                            }
                        },
                        onNoClicked = {
                            onFinish.invoke()
                        },
                        icon = R.drawable.alert_icon
                    )
                }
            }
        }
    }

    /**
     * [This function needs to be called on a separated thread]
     * Executes the inscription of `ramo` to the user inscribed list. Should be called after conflict check.
     */
    private fun inscribeRamoAction(ramo :Ramo) {
        try {
            this.writeLock.lock()
            this.user_ramos.add(ramo)
            this.localDB.ramosDAO().insert(ramo) // assuming we're already in a separate thread
            this.catalog_events.filter { it.ramoNRC == ramo.NRC }
                .forEach { event :RamoEvent ->
                    this.localDB.eventsDAO().insert(event)
                }
            Logf("[DataMaster] Ramo{NRC=%s} inscribed.", ramo.NRC)
        } finally {
            this.writeLock.unlock()
        }
    }

    /* Removes the matching `Ramo` from the user inscribed list. */
    public fun unInscribeRamo(NRC :Int) {
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

    /* Gets the total amount of `crédito` for the user inscribed list. */
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
     * Iterates all user inscribed `Ramo` list and checks if `event` collide with another.
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
            events = this.localDB.eventsDAO().getEventsOfRamo(nrc=ramo.NRC) // TODO: may change to memory query (to `this.user_events`) instead of Room DB query. If so, do not call as an AsyncTask
            //events = this.user_events.filter { it.ramoNRC == ramo.NRC }
            events.forEach { event :RamoEvent ->
                if (! event.isEvaluation()) {
                    when(event.dayOfWeek) {
                        DayOfWeek.MONDAY    -> { results[DayOfWeek.MONDAY]?.add(event) }
                        DayOfWeek.TUESDAY   -> { results[DayOfWeek.TUESDAY]?.add(event) }
                        DayOfWeek.WEDNESDAY -> { results[DayOfWeek.WEDNESDAY]?.add(event) }
                        DayOfWeek.THURSDAY  -> { results[DayOfWeek.THURSDAY]?.add(event) }
                        DayOfWeek.FRIDAY    -> { results[DayOfWeek.FRIDAY]?.add(event) }
                        else -> { Logf("[DataMaster] WARNING: unknown day for this event: %s", event) } // ignored
                    }
                }
            }
        }
        return results
    }

    public fun exportEventsV2(context :Context, ramos :List<Ramo> = this.user_ramos) {
        fun streamWriteFile(content :String, stream :OutputStream) {
            try {
                stream.write(content.toByteArray())
                stream.close()
            }
            catch (e :IOException) {
                Logf("[DataMaster] Failed to export ICS file. %s", e)
            }
        }

        val root :String = Environment.getExternalStorageDirectory().toString()
        val dir :File = File(root + "/abcdefg")
        if (! dir.exists()) dir.mkdirs()
        val filename :String = "temp.ics"
        val icsFile :File = File(dir, filename)
        if (icsFile.exists()) icsFile.delete()
        else icsFile.createNewFile()
        try {
            Logf("Creating/overriding file at %s...", icsFile.absolutePath)
            streamWriteFile(content=this.buildIcsContent(ramos), stream=FileOutputStream(icsFile))
        } catch (e :Exception) {
            Logf("[DataMaster] Error: couldn't write to file. %s", e)
        }

        Logf("Trying to open the file... (exists=%s)", icsFile.exists())
        context.sendBroadcast(
            Intent( Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()) )
        )
    }

    /**
     * Exports the tests and exams for all courses in `ramos` as events to the user's default calendar app.
     */
    public fun exportCalendarEvents(context :Context, ramos :List<Ramo> = this.user_ramos) {
        val icsFile :File

        /* creating and saving temporal ICS (iCalendar) file */
        val saveFolder :String = "Download"

        fun streamWriteFile(content :String, stream :OutputStream) {
            try {
                stream.write(content.toByteArray())
                stream.close()
            }
            catch (e :IOException) {
                Logf("[DataMaster] Failed to export ICS file. %s", e)
            }
        }

        val fileMetadata :ContentValues = ContentValues()
        fileMetadata.put(MediaStore.Downloads.MIME_TYPE, "text/calendar")
//        fileMetadata.put(MediaStore.Images.Media.MIME_TYPE, "text/calendar")
//        fileMetadata.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            fileMetadata.put(MediaStore.Downloads.RELATIVE_PATH, saveFolder)
            fileMetadata.put(MediaStore.Downloads.IS_PENDING, true)
//            fileMetadata.put(MediaStore.Images.Media.RELATIVE_PATH, saveFolder)
//            fileMetadata.put(MediaStore.Images.Media.IS_PENDING, true)

            val uri :Uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, fileMetadata)!!
            icsFile = File(uri.path!!)
//            icsFile = File(context.applicationContext.getExternalFilesDir(null), "temp.ics")
            streamWriteFile(content=this.buildIcsContent(ramos), stream=context.contentResolver.openOutputStream(uri)!!)
            context.contentResolver.update(uri, fileMetadata, null, null)
        }
        else {
            val directory = File( "%s/%s".format(Environment.getExternalStorageDirectory().toString(), saveFolder) )
            if (! directory.exists()) { directory.mkdirs() }

            val fileName :String = "%s.ics".format(System.currentTimeMillis().toString())
            icsFile = File(directory, fileName)
            streamWriteFile(content=this.buildIcsContent(ramos), stream=FileOutputStream(icsFile))
            fileMetadata.put(MediaStore.Downloads.DATA, icsFile.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileMetadata)
        }
        Logf("[DataMaster] iCalendar/ICS file created at %s", icsFile.path)

        /* opening created ICS file with default calendar app */
        this.extractEventsFromICS(context, icsFile)
    }

    /**
     * Opens the iCalendar file with the default calendar app on the device.
     * @param context Needs a context in order to launch the "open with"-like activity.
     * @param icsFile The existing and right-formatted iCalendar file, containing the events.
     */
    private fun extractEventsFromICS(context :Context, icsFile :File) {
        Logf("[DataMaster] Opening iCalendar/ICS file...")

        // TODO: fix crash when passing the URI to an external app. Implement this: https://stackoverflow.com/a/38858040

        Logf("Trying to open file at %s (name=%s, exists=%s)", icsFile.absolutePath,  icsFile.toURI().toString(), icsFile.exists())

        val intent :Intent = Intent(Intent.ACTION_VIEW)
//        intent.data = Uri.fromFile(icsFile)
        intent.data = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", icsFile)
        intent.type = "text/calendar"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser :Intent = Intent.createChooser(intent, "Elíjase la app para abrir: ")

        if (chooser.resolveActivity(context.packageManager) != null) { // opening events if the user has at least one calendar app
            context.startActivity(chooser)
        } else {
            Logf("[DataMaster] Could not open ICS file for events. There is no app installed on the device which can handle calendar events.")
            context.infoDialog(
                title="Error al exportar eventos de calendario",
                message="Parece que ud. no tiene una aplicación para abrir eventos de calendario"
            )
        }
    }

    /**
     * [This function needs to be called on a separated thread]
     * Generates the ICS file contents and returns it.
     */
    private fun buildIcsContent(ramos :List<Ramo>) : String {

        // TODO: make sure this function generates a valid iCalendar file

        val fileHeader :String = """BEGIN:VCALENDAR
PRODID:-//Google Inc//Google Calendar 70.9054//EN
VERSION:2.0
CALSCALE:GREGORIAN
METHOD:PUBLISH
X-WR-CALNAME:CalendarioEvaluaciones
X-WR-TIMEZONE:America/Santiago"""
        var fileBody :String = ""
        val fileFooter :String = "END:VCALENDAR"

        var _year      :String
        var _month     :String
        var _day       :String
        var _startTime :String
        var _endTime   :String

        /* processing evaluations... */
        ramos.forEach { ramo :Ramo ->
            this.user_events.filter { it.ramoNRC==ramo.NRC }.forEach { event :RamoEvent ->
                if (event.isEvaluation()) {
                    _year = event.date!!.year.toString()
                    _month = event.date!!.month.toString()
                    _day = event.date!!.dayOfMonth.toString()
                    _startTime = event.startTime.toString().replace(":", "")
                    _endTime = event.endTime.toString().replace(":", "")

                    fileBody += """
BEGIN:VEVENT
DTSTART;TZID=America/Santiago:${_year}${_month}${_day}T${_startTime}00
DTEND;TZID=America/Santiago:${_year}${_month}${_day}T${_endTime}00
DESCRIPTION:[${ramo.materia} ${ramo.NRC}] ${ramo.nombre}
STATUS:CONFIRMED
SUMMARY:${SpanishToStringOf.ramoEventType(eventType=event.type, shorten=false)} ${ramo.nombre} (${event.startTime})
END:VEVENT
"""
                }
            }
        }

        return "%s%s%s".format(fileHeader, fileBody, fileFooter)
    }
}