package com.ifgarces.tomaramosuandes

import android.os.AsyncTask
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.Logf


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
        clear_database :Boolean,
        onSuccess :() -> Unit,
        onInternetError :() -> Unit,
        onRoomError :() -> Unit
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

                // TODO: load/clear user collection(s) of `Ramo` (Room DB)

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

    public fun findRamoByNRC(nrc :Int) : Ramo? {
        for (ramo :Ramo in this.catalog) {
            if (ramo.NRC == nrc) { return ramo }
        }
        return null
    }

    public fun getUserCreditsCount() : Int {
        var creditosTotal :Int = 0
        for (cc :Ramo in this.userRamos) {
            creditosTotal += cc.créditos
        }
        return creditosTotal
    }

    /* Creates the ICS file for the tests and exams for all courses in `ramos`, storing it at `savePath` */
    public fun exportICS(ramos :List<Ramo>, savePath :String) {
        // TODO: create and save ICS file with all `Ramo`s tests.
    }
}