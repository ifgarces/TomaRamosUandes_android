package com.ifgarces.tomaramosuandes

import android.os.AsyncTask
import com.ifgarces.tomaramosuandes.DataMaster.catalog
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import com.ifgarces.tomaramosuandes.utils.CSVWorker
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.WebManager
import java.time.DayOfWeek


/**
 * Handles the database.
 * @property catalog Contains the collection of `Ramo` available for the current period.
 */
object DataMaster {
    private lateinit var catalog   :List<Ramo>;        fun getCatalog() = this.catalog
    private lateinit var userRamos :MutableList<Ramo>; fun getUserRamos() = this.userRamos

    /**
     * Fetches the `catalog` from a the internet, calling `WebManager`.
     * @param clear_database If true, deletes the local Room database.
     * @param onSuccess Executed when successfully finished database initialization.
     * @param onInternetError Executed when the data file can't be fetched or its elements are invalid somehow.
     * @param onRoomError Executed when it is not possible to load user's local Room database.
     */
    fun init(
        clear_database  :Boolean,
        onSuccess       :() -> Unit,
        onInternetError :() -> Unit,
        onRoomError     :() -> Unit
    ) {
        this.catalog = listOf()
        this.userRamos = mutableListOf()
        AsyncTask.execute {
            try {
                Logf("[DataMaster] Fetching CSV catalog data...")
                val csv_body :String = WebManager.FetchOnlineDataCSV()

                Logf("[DataMaster] Parsing CSV...")
                this.catalog = CSVWorker.parseCSV(csv_lines=csv_body.split("\n"))!!
                Logf("[DataMaster] CSV parsing complete. Catalog size: %d", this.catalog.count())

                // TODO: Room DB

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
        this.userRamos.add(ramo)
        // TODO: update Room DB
    }

    public fun deleteUserRamo(NRC :Int) {
        for (ramo :Ramo in this.userRamos) {
            if (ramo.NRC == NRC) {
                this.userRamos.remove(ramo)
                // TODO: update Room DB
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

    /* Gets all the non-evaluation events, filtered by each non-weekend `DayOfWeek` */
    public fun getAgendaEvents(ramos :List<Ramo> = this.userRamos) : Map<DayOfWeek, List<RamoEvent>> {
        val results :MutableMap<DayOfWeek, MutableList<RamoEvent>> = mutableMapOf(
            DayOfWeek.MONDAY to mutableListOf(),
            DayOfWeek.TUESDAY to mutableListOf(),
            DayOfWeek.WEDNESDAY to mutableListOf(),
            DayOfWeek.THURSDAY to mutableListOf(),
            DayOfWeek.FRIDAY to mutableListOf()
        )
        for (ramo :Ramo in ramos) {
            for (event :RamoEvent in ramo.events) {
                if ( (event.type != RamoEventType.PRBA) && (event.type != RamoEventType.EXAM) ) {
                    when(event.dayofWeek) {
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