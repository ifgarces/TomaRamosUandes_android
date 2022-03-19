package com.ifgarces.tomaramosuandes.utils

import android.app.Activity
import androidx.room.Room
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.local_db.LocalRoomDB
import com.ifgarces.tomaramosuandes.models.CareerAdvice
import com.ifgarces.tomaramosuandes.models.PrettyHyperlink
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.UserStats
import com.ifgarces.tomaramosuandes.networking.FirebaseMaster
import java.time.DayOfWeek
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass


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
    @Volatile
    private lateinit var user_stats :UserStats
    @Volatile
    private lateinit var user_ramos :MutableList<Ramo>
    @Volatile
    private lateinit var user_events :MutableList<RamoEvent>

    // Locks for concurrency
    private lateinit var ramosLock :ReentrantLock
    private lateinit var eventsLock :ReentrantLock

    // Getters (memory)
    fun getCatalogRamos() = this.catalog_ramos
    fun getCatalogEvents() = this.catalog_events
    fun getUserStats() = this.user_stats
    fun getUserRamos() = this.user_ramos

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
        this.catalog_ramos = listOf()
        this.user_ramos = mutableListOf()
        this.user_events = mutableListOf()
        this.ramosLock = ReentrantLock()
        this.eventsLock = ReentrantLock()

        if (forceLoadOfflineCSV) {
            // Connection with remote database failed, using local offline catalog
            // instead
            Logf.warn(this::class, "WARNING: forcing usage of CSV offline catalog over Firebase online catalog")
            this.loadLocalOfflineCatalog(activity)

            Executors.newSingleThreadExecutor().execute {
                this.loadRoomUserData(
                    activity = activity,
                    onSuccess = {
                        if (clearDatabase) {
                            Logf.warn(this::class, "WARNING: clearing Room local database")
                            this.clearUserRamos()
                        }
                        onSuccess.invoke()
                    },
                    onFailure = { e :Exception ->
                        onRoomError.invoke(e)
                    }
                )
            }
        } else {
            // Getting latest catalog and storing in memory
            this.getCatalogFromFirebase(
                onSuccess = {
                    Executors.newSingleThreadExecutor().execute {
                        // Loading local user data, e.g. inscribed `Ramo`s
                        this.loadRoomUserData(
                            activity = activity,
                            onSuccess = {
                                // Clearing local Room database, if desired (debugging only!)
                                if (clearDatabase) {
                                    Logf.warn(this::class, "WARNING: clearing Room local database")
                                    this.clearUserRamos()
                                }
                                onSuccess.invoke()
                            },
                            onFailure = { e :Exception ->
                                onRoomError.invoke(e)
                            }
                        )
                    }
                },
                onFailure = { e :Exception ->
                    // Connection with remote database failed, using local offline catalog instead
                    this.loadLocalOfflineCatalog(activity)
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
            this.user_ramos.clear()
            this.user_events.clear()
            this.localDB.ramosDAO().clear()
            this.localDB.eventsDAO().clear()
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
        // Building Room database
        try {
            this.localDB = Room.databaseBuilder(
                activity, LocalRoomDB::class.java, "tomaramosuandes.db"
            ).fallbackToDestructiveMigration().build() // clear DB on missing migration: https://stackoverflow.com/a/60959586/12684271
        } catch (e :Exception) {
            Logf.error(
                this::class, "Error: could not load local Room database. %s", e.stackTraceToString()
            )
            FirebaseMaster.reportErrorToCrashlytics(e)
            onFailure.invoke(e)
        }

        // Loading database
        this.initUserStats(
            onFinish = {
                // Loading local database (user's inscribed Ramos)
                this.user_ramos = this.localDB.ramosDAO().getAllRamos().toMutableList()
                this.user_events = this.localDB.eventsDAO().getAllEvents().toMutableList()

                // Checking consistency of current catalog with existing data inscribed by the user found in
                // local storage
                val (consistencyIssuesRamos, consistencyIssuesEvents) = this.checkUserAndCatalogConsistency()
                Logf.debug(this::class, "%d consistency issues found (%d events involved)", consistencyIssuesRamos.count(), consistencyIssuesEvents.count())
                if (consistencyIssuesRamos.count() > 0) {
                    val consistencySummaryReport :String = "- %s".format(
                        consistencyIssuesRamos.map { "%s (NRC %d)".format(it.nombre, it.NRC) }
                            .joinToString("\n- ")
                    )
                    val consistencyDetailedReport :String = consistencyIssuesEvents.map {
                        "» [CATÁLOGO] %s\n» [DATOS GUARDADOS] %s\n".format(it.first.toShortString(), it.second.toShortString())
                    }.joinToString("\n")
                    activity.runOnUiThread {
                        // Invoking `onSuccess` after that dialog is dismissed, so we can
                        // avoid the user from navigating to the next activity until they
                        // dismiss the dialog (otherwise it wouldn't be visible, as the next
                        // activity would be on top)
                        activity.infoDialog(
                            title = "Alerta de consistencia de ramos",
                            message = """\
Se encontraron ramos tomados por ud. que son inconsistentes con el catálogo vigente. Esto se debe \
a que el catálogo fue actualizado y algún ramo suyo fue modificado de alguna forma en él:
${consistencySummaryReport}

${
    if (consistencyIssuesEvents.count() > 0) {
"""
Detalle de eventos no consistentes:

${consistencyDetailedReport}""".multilineTrim()
    } else {
        "" // this happens when the amount of events between conflictive `Ramo`s is different, or the `Ramo`s theirself differ
    }
}

La información del catálogo vigente reemplazará a la antigua, automáticamente.""".multilineTrim(),
                            onDismiss = {
                                // Removing `Ramo`s (and their events) conflicting with catalog
                                Logf.debug(
                                    this::class,
                                    "Clearing user data from unconsistent Ramos with catalog"
                                )
                                consistencyIssuesRamos.forEach { conflictiveRamo :Ramo ->
                                    this.unInscribeRamo(conflictiveRamo.NRC)
                                }

                                Logf.debug(
                                    this::class,
                                    "Re-inscribing new catalog Ramos accordingly"
                                )
                                var catalogRamo :Ramo?
                                consistencyIssuesRamos.forEach { conflictiveRamo :Ramo ->
                                    catalogRamo = this.findRamo(
                                        NRC=conflictiveRamo.NRC, searchInUserList=false
                                    )
                                    if (catalogRamo != null) {
                                        this.inscribeRamoAction(ramo = catalogRamo!!, onFinish = {})
                                    }
                                }

                                onSuccess.invoke()
                            }
                        )
                    }
                    return@initUserStats
                }

                Logf.debug(
                    this::class, "Recovered user local data: %s ramos (with %d events)",
                    this.user_ramos.count(), this.user_events.count()
                )
                onSuccess.invoke()
            }
        )
    }

    /**
     * Initializes `UserStats` from local Room database. This was originally private and called only
     * from `Datamaster.init`, but in order for the night theme setting to work on `MainActivity`,
     * this method is intended to be called there.
     * @param onFinish Callback invoked after database transaction finished successfully.
     */
    private fun initUserStats(onFinish :() -> Unit) {
        Executors.newSingleThreadExecutor().execute {
            this.localDB.userStatsDAO().getStats().let { stats :List<UserStats> ->
                if (stats.count() == 0) { // happens at first run of the app since installation
                    // We immediately set `firstRunOfApp` to false in database, but keep it `true`
                    // in memory, only for this run (first run)
                    this.user_stats = UserStats()
                    this.user_stats.firstRunOfApp = false
                    this.localDB.userStatsDAO().insert(this.user_stats)
                    this.user_stats.firstRunOfApp = true
                } else {
                    this.user_stats = stats.first()
                }
                Logf.debug(this::class, this.user_stats.toString())
                onFinish.invoke()
            }
        }
    }

    /**
     * Updates `UserStats` in database (and memory) in order to preserve the user preference on app
     * theme. The night mode setting is switched.
     * @param onFinish Callback invoked after database transaction finished successfully.
     */
    public fun toggleNightMode(onFinish :() -> Unit) {
        Executors.newSingleThreadExecutor().execute {
            this.user_stats.nightModeOn = !this.user_stats.nightModeOn
            this.localDB.userStatsDAO().update(this.user_stats)
            onFinish.invoke()
        }
    }

    /**
     * Updated the visibility status for a section of `DashboardFragment`. Requires the class for
     * the model involved in each section. This is just for not using a hardcoded string.
     * @param modelClass Class under `models` package.
     */
    public fun toggleSectionCollapsed(modelClass :KClass<*>) {
        Executors.newSingleThreadExecutor().execute {
            when (modelClass) {
                RamoEvent::class -> {
                    this.user_stats.dashboardEvalsSectionCollapsed = !this.user_stats.dashboardEvalsSectionCollapsed
                }
                PrettyHyperlink::class -> {
                    this.user_stats.dashboardLinksSectionCollapsed = !this.user_stats.dashboardLinksSectionCollapsed
                }
                CareerAdvice::class -> {
                    this.user_stats.dashboardAdvicesSectionCollapsed = !this.user_stats.dashboardAdvicesSectionCollapsed
                }
                else -> {
                    throw Exception("Invalid class '%s' passed on DataMaster.toggleSectionCollapsed".format( // only happens on dumb coding mistake
                        modelClass.simpleName
                    ))
                }
            }
            this.localDB.userStatsDAO().update(this.user_stats)
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
                        this.catalog_ramos = gotRamos
                        this.catalog_events = gotEvents
                        Logf.debug(
                            this::class, "Fetched %d ramos and %d events from Firebase",
                            this.catalog_ramos.count(), this.catalog_events.count()
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

    /**
     * Parses the local CSV for loading catalog with no internet connection, and stores `Ramo`s and
     * `RamoEvent`s in memory.
     */
    private fun loadLocalOfflineCatalog(activity :Activity) {
        // Connection with remote database failed, using offline catalog
        Logf.debug(this::class, "Loading offline catalog from CSV file...")
        val (offlineRamos :List<Ramo>, offlineEvents :List<RamoEvent>) = CsvHandler.parseCsv(
            fileStream = activity.assets.open(CsvHandler.CSV_FILE_NAME)
        )
        this.catalog_ramos = offlineRamos
        this.catalog_events = offlineEvents

        Logf.debug(
            this::class, "CSV parsing complete, got %d ramos and %d events",
            this.catalog_ramos.count(), this.catalog_events.count()
        )
    }

    /**
     * Checks whether the user inscribed `Ramo`s (and their `RamoEvent`s) are consistent with the
     * current `DataMaster`'s catalog, i.e. whether all of them exist in the current catalog and
     * there are no differences (due Uandes' engineering faculty catalog updates).
     * Warning: time execution will grow explosively with lenght of `catalog_ramos` and
     * `catalog_events`...
     * @returns A pair where the first item is a collection of all the user `Ramo`s conflicting with
     * the catalog data. If that set is empty, no consistency issues exist. The second item of the
     * pair is the list of conflicting `RamoEvent` pairs.
     */
    private fun checkUserAndCatalogConsistency() :Pair<Set<Ramo>, List<Pair<RamoEvent, RamoEvent>>> {
        val consistencyIssuesSummary :MutableSet<Ramo> = mutableSetOf() // `Ramo`s affected
        val consistencyIssuesDetail :MutableList<Pair<RamoEvent, RamoEvent>> = mutableListOf() // `RamoEvent`s colliding between them
        this.catalog_ramos.forEach { catalogRamo :Ramo ->
            this.user_ramos.forEach { userRamo :Ramo ->
                if (userRamo.NRC == catalogRamo.NRC && (
                    userRamo.conectLiga != catalogRamo.conectLiga ||
                    userRamo.créditos != catalogRamo.créditos ||
                    userRamo.curso != catalogRamo.curso ||
                    userRamo.listaCruzada != catalogRamo.listaCruzada ||
                    userRamo.materia != catalogRamo.materia ||
                    userRamo.nombre != catalogRamo.nombre ||
                    userRamo.planEstudios != catalogRamo.planEstudios ||
                    userRamo.profesor != catalogRamo.profesor ||
                    userRamo.sección != catalogRamo.sección
                )) {
                    // This will run when there's a user-inscribed Ramo that does not perfectly
                    // match the current catalog and therefore is not valid [anymore]
                    Logf.warn(
                        this::class,
                        "Found a user-inscribed Ramo that is not consistent with catalog, respectively: %s != %s",
                        userRamo, catalogRamo
                    )
                    consistencyIssuesSummary.add(userRamo)
                }
            }
        }

        // If the `Ramo`s itself are conflictive, we won't bother checking on the events. This is
        // not likely to happen unless the user has inscribed `Ramo`s from a previous academic
        // period
        if (consistencyIssuesSummary.count() > 0) {
            return Pair(consistencyIssuesSummary, listOf())
        }

        // Sorting `RamoEvent`s by ID, but relative to the `Ramo` they belong to, so as to better
        // check for consistency issues between (online) catalog data and local user data
        val sorted_catalog_events :List<List<RamoEvent>> = this.catalog_ramos
            .filter { this.user_ramos.contains(it) } // filtering `Ramo`s that are not inscribed by the user
            .sortedByDescending { it.NRC }
            .map {
                this.getEventsOfRamo(ramo = it, searchInUserList = false).sortedBy { it.ID }
            }
        val sorted_user_events :List<List<RamoEvent>> = this.user_ramos
            .sortedByDescending { it.NRC }
            .map {
                this.getEventsOfRamo(ramo = it, searchInUserList = true).sortedBy { it.ID }
            }

        // At this point, `sorted_catalog_events` and `sorted_user_events` should have equal size
        //assert(sorted_catalog_events.count() == sorted_user_events.count())

        // Defining auxiliary variables
        var catRamoEvents :List<RamoEvent>
        var usrRamoEvents :List<RamoEvent>
        var catEvent :RamoEvent
        var usrEvent :RamoEvent
        var catRamoEventsCount :Int
        var usrRamoEventsCount :Int
        var ramo :Ramo

        // Now we can compare data for matching `Ramo`s, as the `RamoEvent`s are sorted relatively
        // to the `Ramo` they belong to.
        for (ramoIndex :Int in (0 until sorted_catalog_events.count())) {
            catRamoEvents = sorted_catalog_events[ramoIndex]
            usrRamoEvents = sorted_user_events[ramoIndex]
            catRamoEventsCount = catRamoEvents.count()
            usrRamoEventsCount = usrRamoEvents.count()
            ramo = this.findRamo(NRC = catRamoEvents.first().ramoNRC, searchInUserList = true)!!
            if (catRamoEventsCount != usrRamoEventsCount) { // the number of events doesn't match, add to consistency issues
                Logf.debug(
                    this::class,
                    "Amount of events for Ramo '%s' (NRC %s) differs between catalog (%d) and local user inscribed data (%d)".format(
                    ramo.nombre, ramo.NRC, catRamoEventsCount, usrRamoEventsCount
                ))
                consistencyIssuesSummary.add(ramo)
            } else { // checking differency between individual events
                for (eventIndex :Int in (0 until catRamoEventsCount)) {
                    catEvent = catRamoEvents[eventIndex]
                    usrEvent = usrRamoEvents[eventIndex]
                    if (
                        catEvent.type != usrEvent.type ||
                        catEvent.location != usrEvent.location ||
                        catEvent.dayOfWeek != usrEvent.dayOfWeek ||
                        catEvent.startTime != usrEvent.startTime ||
                        catEvent.endTime != usrEvent.endTime ||
                        catEvent.date != usrEvent.date
                    ) {
                        Logf.debug(
                            this::class,
                            "Event #%d of ramo '%s' (NRC %s) differs between catalog and local user data.\n%s != %s".format(
                            eventIndex, ramo.nombre, ramo.NRC, catEvent, usrEvent
                        ))
                        consistencyIssuesSummary.add(ramo)
                        consistencyIssuesDetail.add(Pair(catEvent, usrEvent))
                    }
                }
            }
        }

        return Pair(consistencyIssuesSummary, consistencyIssuesDetail)
    }

    /**
     * Returns all the `RamoEvent`s of all inscribed `Ramo`s that are a test or exam (not classes, etc.).
     */
    public fun getUserEvaluations() :List<RamoEvent> {
        return this.user_events.filter { it.isEvaluation() }
    }

    /**
     * Returns all the events of `ramo`.
     * @param searchInUserList If true, will look into events incribed by user only, otherwise it
     * will search along all the events of the catalog only.
     */
    public fun getEventsOfRamo(ramo :Ramo, searchInUserList :Boolean) :List<RamoEvent> {
        return if (searchInUserList) {
            this.user_events.filter { it.ramoNRC == ramo.NRC }
        } else {
            this.catalog_events.filter { it.ramoNRC == ramo.NRC }
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
                this.ramosLock.lock()
                this.user_ramos.forEach {
                    if (it.NRC == NRC) return it
                }
            } finally {
                this.ramosLock.unlock()
            }
        } else {
            this.catalog_ramos.forEach {
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

        this.getEventsOfRamo(ramo = ramo, searchInUserList = false).forEach { event :RamoEvent ->
            this.getConflictsOf(event).forEach { conflictedEvent :RamoEvent ->
                conflictReport += "• %s\n".format(conflictedEvent.toShortString())
            }
        }

        if (conflictReport == "") { // no conflict was found
            this.inscribeRamoAction(
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
                        this.inscribeRamoAction(
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
                                    this::class, "Unknown day for this event: %s", event
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