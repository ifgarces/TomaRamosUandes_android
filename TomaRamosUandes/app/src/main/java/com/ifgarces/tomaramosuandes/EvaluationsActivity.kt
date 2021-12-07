package com.ifgarces.tomaramosuandes

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.ifgarces.tomaramosuandes.adapters.RamoEventsExpandedAdapter
import com.ifgarces.tomaramosuandes.utils.*


class EvaluationsActivity : AppCompatActivity() {

    private class ActivityUI(owner :AppCompatActivity) {
        val topBar             :MaterialToolbar = owner.findViewById(R.id.evals_topbar)
        val eventsRecycler     :RecyclerView = owner.findViewById(R.id.evals_eventsRecycler)
        val eventsExportButton :Button = owner.findViewById(R.id.evals_exportEventsButton)
    }
    private lateinit var UI :ActivityUI

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_evaluations)
        this.UI = ActivityUI(owner=this)

        UI.eventsRecycler.layoutManager = LinearLayoutManager(this)
        UI.eventsRecycler.adapter = RamoEventsExpandedAdapter(data=DataMaster.getUserRamos())
        UI.eventsExportButton.setOnClickListener { this.exportEvaluations() }
        UI.topBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_help -> { this.showHelp() }
                else -> {
                    Logf(this::class, "[EvaluationsActivity] Warning: unknown toolbar element pressed (id=%d).", it.itemId)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    /**
     * Converts the evaluations of inscribed ramos into a collection of calendar events and open
     * them with the default calendar app.
     */
    private fun exportEvaluations() {
        this.yesNoDialog(
            title = "Exportar evaluaciones",
            message = "Las pruebas y exámenes de sus ramos tomados serán exportadas al calendario que escoja. ¿Continuar?",
            onYesClicked = {
                CalendarHandler.exportEventsToCalendar(activity=this)
            },
            onNoClicked = {},
            icon = null
        )
    }

    /**
     * Shows an information/help dialog about this view.
     */
    private fun showHelp() {
        this.infoDialog(
            title = "Ayuda - Evaluaciones",
            message = """\
Aquí se ven las pruebas y exámenes de cada ramo que ud. haya tomado. \
Use el botón "Exportar al calendario" para indexar las evaluaciones en su calendario \
(de Google, por ejemplo). \
            """.multilineTrim()
        )
    }
}