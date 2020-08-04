package com.ifgarces.tomaramosuandes

import android.app.Activity
import android.os.AsyncTask
import com.ifgarces.tomaramosuandes.models.Curso
import com.ifgarces.tomaramosuandes.utils.Logf


/* *****************
TODO: enhance modularization (model and controller)
***************** */


/**
 * Handles the database.
 * @property catalog Contains the collection of `Curso` available for the current period.
 */
object DataMaster {
    private lateinit var catalog :List<Curso>

    /**
     * Fetches the `catalog` from a internet resource.
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
        var csv_body :String = ""
        AsyncTask.execute {
            try {
                Logf("[DataMaster] Fetching CSV catalog data...")
                csv_body = WebManager.FetchOnlineDataCSV()

                Logf("[DataMaster] Parsing CSV...")
                this.catalog = CSVWorker.parseCSV(csv_lines=csv_body.split("\n"))!!
                Logf("[DataMaster] CSV parsing complete. Catalog size: %d", this.catalog.count())

                // TODO: load/clear user collection(s) of `Curso` (Room DB)

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

    /* Uses Room database to locally save the given collection of `Curso`. */
    public fun saveCursos(user_cursos :List<Curso>) {}

    public fun getCreditsCountOf(cursos :List<Curso>) : Int {
        var creditsNum :Int = 0
        for (cc :Curso in cursos) {
            creditsNum += cc.créditos
        }
        return creditsNum
    }

    /* Creates the ICS file for the tests and exams for all courses in `cursos`, storing it at `savePath` */
    public fun exportICS(cursos :List<Curso>, savePath :String) {
        // TODO: crear archivo ICS y guardar.
    }
}