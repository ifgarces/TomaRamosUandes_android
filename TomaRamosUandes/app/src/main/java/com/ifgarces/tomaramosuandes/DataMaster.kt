package com.ifgarces.tomaramosuandes

import android.os.AsyncTask
import com.ifgarces.tomaramosuandes.DataMaster.catalog
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.CSVWorker
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.WebManager
import java.time.DayOfWeek
import java.util.concurrent.locks.ReentrantLock


/**
 * Handles the database.
 * @property catalog Contains the collection of `Ramo` available for the current period.
 */
object DataMaster {

    // ----
    // TODO: be sure to manage concurrency for `userRamos` (and `catalog` and)?
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
                val csv_body :String = WebManager.FetchOnlineDataCSV()

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

    public fun findRamo(NRC :Int) : Ramo? {
        for (ramo :Ramo in this.catalog) {
            if (ramo.NRC == NRC) { return ramo }
        }
        return null
    }

    public fun addUserRamo(ramo :Ramo) {
        try {
            this.writeLock.lock()
            this.userRamos.add(ramo)
            // TODO: update Room DB
        } finally {
            this.writeLock.unlock()
        }
    }

    public fun deleteUserRamo(NRC :Int) {
        this.userRamos.forEachIndexed { index :Int, ramo :Ramo ->
            if (ramo.NRC == NRC) {
                try {
                    this.writeLock.lock()
                    this.userRamos.removeAt(index)
                    // TODO: update Room DB
                } finally {
                    this.writeLock.unlock()
                }
            }
        }
    }

    public fun getUserTotalCredits() : Int {
        var creditosTotal :Int = 0
        for (cc :Ramo in this.userRamos) {
            creditosTotal += cc.créditos
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

    /* Iterates all user `Ramo` list and checks if `event` collide with another */
    public fun isTakenEventConflicted(event :RamoEvent) : Boolean {
        this.userRamos.forEach {
            it.events.forEach { ev :RamoEvent ->
                if (ev.ID != event.ID) {
                    if (this.areEventsConflicted(ev, event) == true) { return true }
                }
            }
        }
        return false
    }

    /* Gets all the non-evaluation events, filtered by each non-weekend `DayOfWeek` */
    public fun getEventsByDay(ramos :List<Ramo> = this.userRamos) : Map<DayOfWeek, List<RamoEvent>> {
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

    /* Creates the ICS file for the tests and exams for all courses in `ramos`, storing it at `savePath` */
    public fun exportICS(ramos :List<Ramo>, savePath :String) {
        // TODO: create and save ICS file with all `Ramo`s tests.
    }
}