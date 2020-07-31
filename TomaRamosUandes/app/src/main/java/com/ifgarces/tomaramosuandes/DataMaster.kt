package com.ifgarces.tomaramosuandes

import android.app.Activity
import android.os.AsyncTask
import com.ifgarces.tomaramosuandes.models.Curso
import com.ifgarces.tomaramosuandes.utils.Logf


/* *****************
TODO: enhance modularization (model and controller)
***************** */

object DataMaster {
    private lateinit var catalog :List<Curso>

    fun init(activity : Activity, clear_database :Boolean) {
        this.catalog = listOf()
        var csv_body :String = ""
        AsyncTask.execute {
            try {
                Logf("[DataMaster] Fetching CSV catalog data...")
                csv_body = WebManager.FetchOnlineDataCSV()

                Logf("[DataMaster] Parsing CSV...")
                this.catalog = CSVWorker.parseCSV(activity=activity, csv_contents=csv_body)
                Logf("[DataMaster] CSV parsing complete. Catalog size: %d", this.catalog.count())
            }
            catch (e :java.net.UnknownHostException) {
                Logf("[DataMaster] Could not load online CSV. Details: %s", e)
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