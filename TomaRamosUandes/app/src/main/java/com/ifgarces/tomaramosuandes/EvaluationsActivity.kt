package com.ifgarces.tomaramosuandes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.ifgarces.tomaramosuandes.adapters.RamoEventsExpandedAdapter


class EvaluationsActivity : AppCompatActivity() {

    private object UI {
        lateinit var topBar             :MaterialToolbar
        lateinit var eventsRecycler     :RecyclerView
        lateinit var eventsExportButton :Button

        fun init(owner :AppCompatActivity) {
            this.topBar = owner.findViewById(R.id.evals_topbar)
            this.eventsRecycler = owner.findViewById(R.id.evals_eventsRecycler)
            this.eventsExportButton = owner.findViewById(R.id.evals_exportEventsButton)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_evaluations)
        UI.init(owner=this)

        UI.eventsRecycler.layoutManager = LinearLayoutManager(this)
        UI.eventsRecycler.adapter = RamoEventsExpandedAdapter(data=DataMaster.getUserRamos())
        UI.eventsExportButton.setOnClickListener { this.exportEvents() }
    }

    private fun exportEvents() {
        //DataMaster.exportICS(context=this)
        // TODO: export ICS and inmediatly open it by system, so the user can import the events to their prefered calendar
    }

    private fun showHelp() {

    }
}