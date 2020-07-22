package com.ifgarces.tomaramosuandes

import android.app.Activity
import android.os.AsyncTask
import com.ifgarces.tomaramosuandes.models.Curso
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.toastf


object DataMaster {
    private lateinit var catalog :List<Curso>

    fun init(activity : Activity, clear_database :Boolean) {
        this.catalog = listOf()
        // TODO: fill catalog with data (online)
        AsyncTask.execute {
            try {
                Logf("Got %d characters from online CSV", WebManager.FetchOnlineDataCSV().length)
            }
            catch (e :java.net.UnknownHostException) {
                Logf("Could not load online CSV. Details: %s", e)
            }
        }
    }

    /* Converts the contents of the CSV into a collection of `Curso` */
    private fun processCSV(contents :String) : List<Curso> {
        var result :MutableList<Curso> = arrayListOf()
        return result
    }

    /* Uses Room database to locally save the given collection of `Curso`. */
    public fun SaveCursos(user_cursos :List<Curso>) {
    }

}