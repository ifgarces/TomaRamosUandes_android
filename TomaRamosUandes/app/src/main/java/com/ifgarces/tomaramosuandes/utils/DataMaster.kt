package com.ifgarces.tomaramosuandes.utils

import android.app.Activity
import androidx.room.Room
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.local_db.LocalRoomDB
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.UserStats
import com.ifgarces.tomaramosuandes.networking.FirebaseMaster
import java.time.DayOfWeek
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock


/**
 * Handles the database. This is like the controller in the MVC model. I'm too lazy to use
 * ViewModels instead. Never liked those anyway.
 * @property catalog_ramos Collection of available `Ramo` for the current period.
 * @property catalog_events Collection of available `RamoEvents` for the current period.
 * @property user_ramos Set of inscribed `Ramo` by the user.
 * @property user_events Set of inscribed `RamoEvent` by the user.
 * @property ramosLock Concurrency write lock for `user_ramos`.
 * @property eventsLock Concurrency write lock for `user_events`.
 */
object DataMaster {
    private lateinit var localDB :LocalRoomDB

    // Current catalog
    @Volatile
    private lateinit var catalog_ramos :List<Ramo>
    @Volatile
    private lateinit var catalog_events :List<RamoEvent>

    // User data
    private lateinit var user_stats :UserStats
    @Volatile
    private lateinit var user_ramos :MutableList<Ramo>
    @Volatile
    private lateinit var user_events :MutableList<RamoEvent>

    // Locks for concurrency
    private lateinit var ramosLock :ReentrantLock
    private lateinit var eventsLock :ReentrantLock

    // Getters
    fun getCatalogRamos() = catalog_ramos
    fun getCatalogEvents() = catalog_events
    fun getUserStats() = user_stats
    fun getUserRamos() = user_ramos

    /**
     * Fetches the catalog from a the internet, calling `WebManager` and processes it.
     * @param activity The caller activity, for accessing assets and constant resources.
     * @param clearDatabase **DEBUGGING ONLY** If true, deletes the local Room database. Must be
     * always false on any serious build.
     * @param forceLoadOfflineCSV **DEBUGGING ONLY** For forcing to use the offline catalog from the
     * CSV asset file, e.g. when updating the catalog to a newer version (not the best way to do it
     * though). Must be always false on any serious build.
     * @param onSuccess Callback executed when successfully finished database initialization.
     * @param onFirebaseError Callback for when connection with the remote firebase fails. When this
     * happens, this will be invoked before the offline CSV catalog is parsed.
     * @param onRoomError Callback executed when it is not possible to load user's local Room
     * database (Room build error). This could happen when trying to load outdated `Ramo`s data from
     * a previous app version (on database model change).
     */
    fun init(
        activity :Activity,
        clearDatabase :Boolean,
        forceLoadOfflineCSV :Boolean,
        onSuccess :() -> Unit,
        onFirebaseError :(e :Exception) -> Unit,
        onRoomError :(e :Exception) -> Unit
    ) {
        catalog_ramos = listOf()
        user_ramos = mutableListOf()
        user_events = mutableListOf()
        ramosLock = ReentrantLock()
        eventsLock = ReentrantLock()

        if (forceLoadOfflineCSV) {
            // Connection with remote database failed, using local offline catalog
            // instead
            loadLocalOfflineCatalog(activity)

            Executors.newSingleThreadExecutor().execute {
                loadRoomUserData(
                    activity = activity,
                    onSuccess = {
                        if (clearDatabase) clearUserRamos()
                        onSuccess.invoke()
                    },
                    onFailure = { e :Exception ->
                        onRoomError.invoke(e)
                    }
                )
            }
        } else {
            // Getting latest catalog and storing in memory
            getCatalogFromFirebase(
                onSuccess = {
                    Executors.newSingleThreadExecutor().execute {
                        // Loading local user data, e.g. inscribed `Ramo`s
                        loadRoomUserData(
                            activity = activity,
                            onSuccess = {
                                // Clearing local Room database, if desired (debugging only!)
                                if (clearDatabase) clearUserRamos()
                                onSuccess.invoke()
                            },
                            onFailure = { e :Exception ->
                                onRoomError.invoke(e)
                            }
                        )
                    }
                },
                onFailure = { e :Exception ->
                    // Connection with remote database failed, using local offline catalog
                    // instead
                    loadLocalOfflineCatalog(activity)
                    onFirebaseError.invoke(e)
                }
            )
        }
    }

    /**
     * Wipes out all `Ramo` and `RamoEvent`s from the user inscribed data. This does not affect
     * the catalog. Does not clear `UserStats`.
     */
    public fun clearUserRamos() {
        Executors.newSingleThreadExecutor().execute {
            user_ramos.clear()
            user_events.clear()
            localDB.ramosDAO().clear()
            localDB.eventsDAO().clear()
            Logf.debug(this::class, "Local user-inscribed Ramos and RamoEvents cleared")
        }
    }

    /**
     * [This method must be executed in another thread]
     * Loads local data from Room, ensures locally saved user-inscribed `Ramo`s are consistent with
     * the current available catalog.
     */
    private fun loadRoomUserData(
        activity :Activity,
        onSuccess :() -> Unit,
        onFailure :(e :Exception) -> Unit
    ) {
        // Building Room database. Will fail sometimes when updating the app. Must uninstall it
        // and install the new version manually on the device.
        try {
            localDB = Room.databaseBuilder(
                activity, LocalRoomDB::class.java, Ramo.TABLE_NAME
            ).build()
        } catch (e :Exception) {
            Logf.error(
                this::class, "Error: could not load local Room database. %s", e.stackTraceToString()
            )
            FirebaseMaster.reportErrorToCrashlytics(e)
            onFailure.invoke(e)
        }

        // Loading local database (user's inscribed Ramos)
        user_ramos = localDB.ramosDAO().getAllRamos().toMutableList()
        user_events = localDB.eventsDAO().getAllEvents().toMutableList()

        // Checking consistency of current catalog with existing data inscribed by the user found in
        // local storage
        if (! checkUserAndCatalogConsistency()) {
            Logf.warn(
                this::class,
                "User inscribed ramos are not consistent with the current catalog. User data cleared."
            )
            val oldUserRamosReport :String = "- %s".format(
                user_ramos.map { "%s (NRC %d)".format(it.nombre, it.NRC) }
                    .joinToString("\n- ")
            )
            activity.runOnUiThread {
                activity.infoDialog(
                    title = "Error de sincronización de ramos",
                    message = """\
Se encontraron ramos tomados por ud. que son inconsistentes con el catálogo vigente. Esto se debe \
a que el catálogo fue actualizado y algún ramo suyo fue removido o modificado de alguna forma. \
Los siguientes ramos fueron removidos por esta razón:
${oldUserRamosReport}""".multilineTrim(),
                    onDismiss = {
                        initUserStats()
                        clearUserRamos()
                        onSuccess.invoke()
                    }
                )
            }
            return
        }

        initUserStats()
        Logf.debug(
            this::class,
            "Recovered user local data: %s ramos (with %d events).",
            user_ramos.count(),
            user_events.count()
        )

        onSuccess.invoke()
    }

    /**
     * Initializes `UserStats` from local Room database.
     */
    private fun initUserStats() {
        Executors.newSingleThreadExecutor().execute {
            localDB.userStatsDAO().getStats().let { stats :List<UserStats> ->
                if (stats.count() == 0) { // happens at first run of the app since installation
                    user_stats = UserStats()
                    user_stats.firstRunOfApp = false
                    localDB.userStatsDAO().insert(user_stats)
                    user_stats.firstRunOfApp = true
                } else {
                    user_stats = stats.first()
                    //this@DataMaster.user_stats.firstRunOfApp = false // <- this line should not be necessary if the stats are saved to Room database always on app close.
                }
            }
        }
    }

    /**
     * Fetches latest `Ramo`s and their `RamoEvent`s via Firebase.
     * @param onSuccess Callback for when interaction with Firebase succeded.
     * @param onFailure Callback invoked when interaction with Firebase failed.
     */
    private fun getCatalogFromFirebase(
        onSuccess :() -> Unit,
        onFailure :(e :Exception) -> Unit
    ) {
        Logf.debug(this::class, "Fetching catalog data...")
        FirebaseMaster.getAllRamos(
            onSuccess = { gotRamos :List<Ramo> ->
                FirebaseMaster.getAllRamoEvents(
                    onSuccess = { gotEvents :List<RamoEvent> ->
                        catalog_ramos = gotRamos
                        catalog_events = gotEvents
                        Logf.debug(
                            this::class, "Fetched %d ramos and %d events from Firebase",
                            catalog_ramos.count(), catalog_events.count()
                        )
                        onSuccess.invoke()
                    },
                    onFailure = { e :Exception ->
                        onFailure.invoke(e)
                    }
                )
            },
            onFailure = { e :Exception ->
                onFailure.invoke(e)
            }
        )
    }

    private fun loadLocalOfflineCatalog(activity :Activity) {
        // Connection with remote database failed, using offline catalog
        Logf.debug(this::class, "Loading offline catalog from CSV file...")
        val (offlineRamos :List<Ramo>, offlineEvents :List<RamoEvent>) = CsvHandler.parseCsv(
            fileStream = activity.assets.open(CsvHandler.CSV_FILE_NAME)
        )
        catalog_ramos = offlineRamos
        catalog_events = offlineEvents

        Logf.debug(
            this::class, "CSV parsing complete. Got %d ramos and %d events",
            catalog_ramos.count(), catalog_events.count()
        )
    }

    /**
     * Returns `true` if the user inscribed `Ramo`s (and their `RamoEvent`s) are consistent with the
     * current `DataMaster`'s catalog, i.e. whether all of them exist in the current catalog and
     * there are no differences (due Uandes' SAF catalog updates).
     * Warning: time execution will grow explosively with lenght of `catalog_ramos` and
     * `catalog_events`...
     */
    private fun checkUserAndCatalogConsistency() :Boolean {
        catalog_ramos.forEach { catalogRamo :Ramo ->
            user_ramos.forEach { userRamo :Ramo ->
                if (userRamo.NRC == catalogRamo.NRC && userRamo != catalogRamo) {
                    // This will run when there's a user-inscribed Ramo that does not perfectly
                    // match the current catalog and therefore is not valid [anymore]
                    Logf.warn(
                        this::class,
                        "Found a user-inscribed Ramo that is not consistent with catalog, respectively: %s != %s",
                        userRamo, catalogRamo
                    )
                    return false
                }
            }
        }
        catalog_events.forEach { catalogEvent :RamoEvent ->
            user_events.forEach { userEvent :RamoEvent ->
                if (userEvent.ID == catalogEvent.ID && userEvent != catalogEvent) {
                    // This will run when there's an event of a user-inscribed Ramo that does not
                    // perfectly match the current catalog and therefore is not valid [anymore]
                    Logf.warn(
                        this::class,
                        "Found a user-inscribed RamoEvent that is not consistent with catalog, respectively: %s != %s",
                        userEvent, catalogEvent
                    )
                    return false
                }
            }
        }
        return true
    }

    /**
     * Returns all the `RamoEvent`s of all inscribed `Ramo`s that are a test or exam (not classes, etc.).
     */
    public fun getUserEvaluations() :List<RamoEvent> {
        return user_events.filter { it.isEvaluation() }
    }

    /**
     * Returns all the events of `ramo`.
     * @param searchInUserList If true, will look into events incribed by user, otherwise it will
     * search along all the events of the catalog.
     */
    public fun getEventsOfRamo(ramo :Ramo, searchInUserList :Boolean) :List<RamoEvent> {
        return if (searchInUserList) {
            user_events.filter { it.ramoNRC == ramo.NRC }
        } else {
            catalog_events.filter { it.ramoNRC == ramo.NRC }
        }
    }

    /**
     * Searchs and returns the `Ramo` whose NRC (ID) matches.
     * @param NRC The fiven ID.
     * @param searchInUserList If true, will search just along the user inscribed `Ramo`s.
     * If not, searches along the hole catalog.
     * @return Returns null if not found.
     */
    public fun findRamo(NRC :Int, searchInUserList :Boolean) :Ramo? {
        if (searchInUserList) {
            try {
                ramosLock.lock()
                user_ramos.forEach {
                    if (it.NRC == NRC) return it
                }
            } finally {
                ramosLock.unlock()
            }
        } else {
            catalog_ramos.forEach {
                if (it.NRC == NRC) return it
            }
        }
        return null
    }

    /**
     * Attemps to inscribe `ramo` and add it to the user list. If any event of `ramo` collides with
     * another already inscribed `Ramo`, prompts conflict confirmation dialog and, if the user
     * decides to continue neverdeless, finishes the action.
     * @param onFinish Will be executed when the inscription operation finished (because this is
     * asyncronous).
     */
    public fun inscribeRamo(ramo :Ramo, activity :Activity, onFinish :() -> Unit) {
        var conflictReport :String = ""

        getEventsOfRamo(ramo = ramo, searchInUserList = false).forEach { event :RamoEvent ->
            getConflictsOf(event).forEach { conflictedEvent :RamoEvent ->
                conflictReport += "• %s\n".format(conflictedEvent.toShortString())
            }
        }

        if (conflictReport == "") { // no conflict was found
            inscribeRamoAction(
                ramo = ramo,
                onFinish = {
                    onFinish.invoke()
                }
            )
        } else { // conflict(s) found, asking user confirmation for inscribing
            activity.runOnUiThread {
                activity.yesNoDialog(
                    title = "Conflicto de eventos",
                    message = """\
Advertencia, los siguientes eventos entran en conflicto:

${conflictReport}
¿Tomar ${ramo.nombre} de todas formas?""".multilineTrim(),
                    onYesClicked = {
                        inscribeRamoAction(
                            ramo = ramo,
                            onFinish = {
                                onFinish.invoke()
                            }
                        )
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
     * Executes the inscription of `ramo` to the user inscribed list. Should be called after conflict check.
     */
    private fun inscribeRamoAction(ramo :Ramo, onFinish :() -> Unit) {
        Executors.newSingleThreadExecutor().execute {
            var eventCount :Int = 0
            try {
                ramosLock.lock()
                eventsLock.lock()
                user_ramos.add(ramo)
                localDB.ramosDAO().insert(ramo) // assuming we're already in a separate thread
                getEventsOfRamo(ramo = ramo, searchInUserList = false)
                    .forEach { event :RamoEvent ->
                        eventCount++
                        user_events.add(event)
                        localDB.eventsDAO().insert(event)
                    }
            } finally {
                ramosLock.unlock()
                eventsLock.unlock()
            }
            Logf.debug(
                this::class, "Ramo{NRC=%s}, along %d of its events, were inscribed.",
                ramo.NRC, eventCount
            )
            onFinish.invoke()
        }
    }

    /**
     * Removes the matching `Ramo` from the user inscribed list.
     * @param NRC Primary key of the item to un-inscribe.
     */
    public fun unInscribeRamo(NRC :Int) {
        var eventCount :Int = 0

        val ramo :Ramo? = findRamo(NRC = NRC, searchInUserList = true)
        if (ramo == null) { // kind of dymmy solution, but this may be needed if the user do stuff quickly, due async tasks
            Logf.debug(
                this::class,
                "This Ramo seems to be already deleted and couldn't be found. Aborting."
            )
            return
        }

        getEventsOfRamo(
            ramo = ramo,
            searchInUserList = true
        ).forEach { event :RamoEvent ->
            eventCount++
            try {
                eventsLock.lock()
                user_events.remove(event)
                Executors.newSingleThreadExecutor().execute {
                    localDB.eventsDAO().deleteRamoEvent(id = event.ID)
                }
            } finally {
                eventsLock.unlock()
            }
        }

        try {
            ramosLock.lock()
            // Deleting by this way in order to avoid a very seldom crash caused by `user_ramos`
            // being a Volatile variable.
            val ramoIterator :MutableIterator<Ramo> = user_ramos.iterator()
            var r :Ramo
            while (ramoIterator.hasNext()) {
                r = ramoIterator.next()
                if (r.NRC == NRC) {
                    ramoIterator.remove()
                    Executors.newSingleThreadExecutor().execute {
                        localDB.ramosDAO().deleteRamo(nrc = NRC)
                    }
                    break
                }
            }
        } finally {
            ramosLock.unlock()
        }
        Logf.debug(
            this::class, "Ramo{NRC=%d}, along %d of its events, were un-inscribed.",
            NRC, eventCount
        )
    }

    /**
     * Returns the total amount of `crédito` for the user inscribed list.
     */
    public fun getUserCreditSum() :Int {
        var creditosTotal :Int = 0
        try {
            ramosLock.lock()
            user_ramos.forEach {
                creditosTotal += it.créditos
            }
        } finally {
            ramosLock.unlock()
        }
        return creditosTotal
    }

    /**
     * Checks if the two events are in conflict. Returns true if there exist an overlaping/collision
     * between them. Will return null if invalid data is passed (error), such as if trying to pass
     * an evaluation event with a non-evaluation event, or if their `date` is null (when
     * passing evaluation evaluation).
     */
    public fun areEventsConflicted(ev1 :RamoEvent, ev2 :RamoEvent) :Boolean? {
        val first_isEval :Boolean = ev1.isEvaluation()
        val second_isEval :Boolean = ev2.isEvaluation()
        if (first_isEval && second_isEval) { // comparing evaluation events (date and time)
            if (ev1.date == null || ev2.date == null) return null // error: missing date
            if (ev1.date != ev2.date) return false
        } else if ((!first_isEval) && (!second_isEval)) { // comparing non-evaluation events (dayOfWeek and time)
            if (ev1.dayOfWeek != ev2.dayOfWeek) return false
        } else {
            return null // unconsistent types: comparing evaluation with non-evaluation
        }

        // "does i starts before j finished and i finishes after j starts?"
        return (ev1.startTime <= ev2.endTime) && (ev1.endTime >= ev2.startTime)
    }

    /**
     * Iterates all user inscribed `Ramo` list and checks if `event` collide with another.
     * @return The other events that collide with `event`, including itself (at first position).
     * The list will be empty if there is no conflict.
     */
    public fun getConflictsOf(event :RamoEvent) :List<RamoEvent> {
        val conflicts :MutableList<RamoEvent> = mutableListOf()
        try {
            ramosLock.lock()
            user_ramos.forEach {
                getEventsOfRamo(ramo = it, searchInUserList = true).forEach { ev :RamoEvent ->
                    if (ev.ID != event.ID) {
                        if (areEventsConflicted(ev, event) == true) {
                            conflicts.add(ev)
                        }
                    }
                }
            }
        } finally {
            ramosLock.unlock()
        }
        if (conflicts.count() > 0) {
            conflicts.add(index = 0, element = event)
        } // addint itself, in order to inform to user in conflict dialog
        return conflicts
    }

    /**
     * [This function needs to be called on a another thread]
     * Gets all the non-evaluation events, filtered by each non-weekend `DayOfWeek`.
     */
    public fun getEventsByWeekDay() :Map<DayOfWeek, List<RamoEvent>> {
        val results :MutableMap<DayOfWeek, MutableList<RamoEvent>> = mutableMapOf(
            DayOfWeek.MONDAY to mutableListOf(),
            DayOfWeek.TUESDAY to mutableListOf(),
            DayOfWeek.WEDNESDAY to mutableListOf(),
            DayOfWeek.THURSDAY to mutableListOf(),
            DayOfWeek.FRIDAY to mutableListOf()
        )
        try {
            ramosLock.lock()
            user_ramos.forEach { ramo :Ramo ->
                getEventsOfRamo(ramo = ramo, searchInUserList = true)
                    .forEach { event :RamoEvent ->
                        if (!event.isEvaluation()) {
                            when (event.dayOfWeek) {
                                DayOfWeek.MONDAY ->  results[DayOfWeek.MONDAY]?.add(event)
                                DayOfWeek.TUESDAY -> results[DayOfWeek.TUESDAY]?.add(event)
                                DayOfWeek.WEDNESDAY -> results[DayOfWeek.WEDNESDAY]?.add(event)
                                DayOfWeek.THURSDAY -> results[DayOfWeek.THURSDAY]?.add(event)
                                DayOfWeek.FRIDAY -> results[DayOfWeek.FRIDAY]?.add(event)
                                else -> Logf.warn( // ignored
                                    this::class, "Warning: unknown day for this event: %s", event
                                )
                            }
                        }
                    }
            }
        } finally {
            ramosLock.unlock()
        }

        return results
    }
}