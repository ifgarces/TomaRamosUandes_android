package com.ifgarces.tomaramosuandes

import android.app.Activity
import com.ifgarces.tomaramosuandes.models.Curso


object DataMaster {
    private lateinit var catalog :List<Curso>

    public fun init(activity : Activity, clear_database :Boolean) {
        this.catalog = listOf()
        // ...
    }

    private fun fetchOnlineCSV() {

    }
}