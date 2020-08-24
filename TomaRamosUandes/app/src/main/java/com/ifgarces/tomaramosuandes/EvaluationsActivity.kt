package com.ifgarces.tomaramosuandes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.ifgarces.tomaramosuandes.adapters.RamoEventsExpandedAdapter
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineTrim


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
        UI.topBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_help -> { this.showHelp() }
                else -> {
                    Logf("[EvaluationsActivity] Warning: unknown toolbar element pressed (id=%d).", it.itemId)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun exportEvents() {
        // this.runOnUiThread{ DataMaster.exportICS(context=this) }
        // TODO: export ICS and inmediatly open it by system, so the user can import the events to their prefered calendar
    }

    private fun showHelp() {
        this.infoDialog(
            title = "Ayuda - Evaluaciones",
            message = """
                Aquí se ven las pruebas y exámenes de cada ramo que ud. haya tomado. \
                Use el botón "Exportar al calendario" para indexar las evaluaciones en su calendario \
                (de Google, por ejemplo).
            """.multilineTrim()
        )
    }
}