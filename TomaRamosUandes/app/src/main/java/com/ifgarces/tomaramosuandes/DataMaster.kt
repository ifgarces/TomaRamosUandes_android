package com.ifgarces.tomaramosuandes

import android.app.Activity
import android.os.AsyncTask
import androidx.room.Room
import com.ifgarces.tomaramosuandes.local_db.LocalRoomDB
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.UserStats
import com.ifgarces.tomaramosuandes.utils.*
import java.time.DayOfWeek
import java.util.concurrent.locks.ReentrantLock


/**
 * Handles the database.
 * @property catalog_ramos Contains the collection of available `Ramo` for the current period.
 * @property catalog_events Collection of available `RamoEvents`.
 * @property user_ramos Set of inscribed `Ramo` by the user.
 * @property user_events Set of inscribed `RamoEvent` by the user.
 * @property ramosLock Concurrency write lock for `user_ramos`.
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

    private lateinit var ramosLock :ReentrantLock
    private lateinit var eventsLock :ReentrantLock

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
        this.ramosLock = ReentrantLock()
        this.eventsLock = ReentrantLock()

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
                Logf("[DataMaster] Could not load online CSV (internet connection error).")
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

    /**
     * Returns all the `RamoEvent`s of all inscribed `Ramo`s that are a test or exam (not classes, etc.).
     */
    public fun getUserEvaluations() : List<RamoEvent> {
        return this.user_events.filter { it.isEvaluation() }
    }

    /**
     * Returns all the events of `ramo`.
     * @param searchInUserList If true, will look into events incribed by user, otherwise it will
     * search along all the events of the catalog.
     */
    public fun getEventsOfRamo(ramo :Ramo, searchInUserList :Boolean) : List<RamoEvent> {
        return if (searchInUserList) {
            this.user_events.filter { it.ramoNRC == ramo.NRC }
        } else {
            this.catalog_events.filter { it.ramoNRC == ramo.NRC }
        }
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
     * @param searchInUserList If true, will search just along the user inscribed `Ramo`s.
     * If not, searches along the hole catalog.
     * @return Returns null if not found.
     */
    public fun findRamo(NRC :Int, searchInUserList :Boolean) : Ramo? {
        if (searchInUserList) {
            try {
                this.ramosLock.lock()
                this.user_ramos.forEach {
                    if (it.NRC == NRC) { return it }
                }
            } finally {
                this.ramosLock.unlock()
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

        this.getEventsOfRamo(ramo=ramo, searchInUserList=false).forEach { event :RamoEvent ->
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
                            this.inscribeRamoAction(ramo) // need this AsyncTask form in order to be certain to call `onFinish` AFTER `ramo` is inscribed into the database.
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

    /**
     * [This function needs to be called on a separated thread]
     * Executes the inscription of `ramo` to the user inscribed list. Should be called after conflict check.
     */
    private fun inscribeRamoAction(ramo :Ramo) {
        try {
            this.ramosLock.lock()
            this.eventsLock.lock()
            this.user_ramos.add(ramo)
            this.localDB.ramosDAO().insert(ramo) // assuming we're already in a separate thread
            this.getEventsOfRamo(ramo=ramo, searchInUserList=false)
                .forEach { event :RamoEvent ->
                    this.user_events.add(event)
                    this.localDB.eventsDAO().insert(event)
                }
            Logf("[DataMaster] Ramo{NRC=%s} inscribed.", ramo.NRC)
        } finally {
            this.ramosLock.unlock()
            this.eventsLock.unlock()
        }
    }

    /**
     * Removes the matching `Ramo` from the user inscribed list.
     * @param NRC Primary key of the item to un-inscribe.
     */
    public fun unInscribeRamo(NRC :Int) {
        this.getEventsOfRamo(
            ramo = this.findRamo(NRC=NRC, searchInUserList=true)!!,
            searchInUserList = true
        ).forEach { event :RamoEvent ->
            try {
                this.eventsLock.lock()
                this.user_events.remove(event)
                AsyncTask.execute { this.localDB.eventsDAO().deleteRamoEvent(id=event.ID) }
            } finally {
                this.eventsLock.unlock()
            }
        }
        Logf("[DataMaster] Ramo{NRC=%s} un-inscribed.", NRC)

        try {
            this.ramosLock.lock()
            this.user_ramos.forEachIndexed { index :Int, ramo :Ramo ->
                if (ramo.NRC == NRC) {
                    this.user_ramos.removeAt(index)
                    AsyncTask.execute { this.localDB.ramosDAO().deleteRamo(nrc=NRC) }
                }
            }
        } finally {
            this.ramosLock.unlock()
        }
    }

    /**
     * Returns the total amount of `crédito` for the user inscribed list.
     */
    public fun getUserCreditSum() : Int {
        var creditosTotal :Int = 0
        try {
            this.ramosLock.lock()
            this.user_ramos.forEach {
                creditosTotal += it.créditos
            }
        } finally {
            this.ramosLock.unlock()
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
     * Iterates all user inscribed `Ramo` list and checks if `event` collide with another.
     * @return The other events that collide with `event`, including itself (at first position).
     * The list will be empty if there is no conflict.
     */
    public fun getConflictsOf(event :RamoEvent) : List<RamoEvent> {
        val conflicts :MutableList<RamoEvent> = mutableListOf()
        try {
            this.ramosLock.lock()
            this.user_ramos.forEach {
                this.getEventsOfRamo(ramo=it, searchInUserList=true).forEach { ev :RamoEvent ->
                    if (ev.ID != event.ID) {
                        if (this.areEventsConflicted(ev, event) == true) { conflicts.add(ev) }
                    }
                }
            }
        }
        finally {
            this.ramosLock.unlock()
        }
        if (conflicts.count() > 0) { conflicts.add(index=0, element=event) } // addint itself, in order to inform to user in conflict dialog
        return conflicts
    }

    /**
     * [This function needs to be called on a separated thread]
     * Gets all the non-evaluation events, filtered by each non-weekend `DayOfWeek`
     */
    public fun getEventsByWeekDay() : Map<DayOfWeek, List<RamoEvent>> {
        val results :MutableMap<DayOfWeek, MutableList<RamoEvent>> = mutableMapOf(
            DayOfWeek.MONDAY to mutableListOf(),
            DayOfWeek.TUESDAY to mutableListOf(),
            DayOfWeek.WEDNESDAY to mutableListOf(),
            DayOfWeek.THURSDAY to mutableListOf(),
            DayOfWeek.FRIDAY to mutableListOf()
        )
        try {
            this.ramosLock.lock()
            this.user_ramos.forEach { ramo :Ramo ->
                this.getEventsOfRamo(ramo=ramo, searchInUserList=true).forEach { event :RamoEvent ->
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
        } finally {
            this.ramosLock.unlock()
        }

        return results
    }
}