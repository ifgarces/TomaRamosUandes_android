package com.ifgarces.tomaramosuandes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.adapters.RamoEventsExpandedAdapter
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.CalendarHandler
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import com.ifgarces.tomaramosuandes.utils.yesNoDialog


class EvaluationsFragment : Fragment() {

    private class FragmentUI(owner :View) {
        val eventsRecycler     :RecyclerView = owner.findViewById(R.id.evals_eventsRecycler)
        val eventsExportButton :Button = owner.findViewById(R.id.evals_exportEventsButton)
    }

    private lateinit var UI :FragmentUI

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(R.layout.fragment_evaluations, container, false)
        this.UI = FragmentUI(owner = fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            homeActivity.setBottomNavItemSelected(this::class)
            homeActivity.setTopToolbarValues(
                title = "Evaluaciones",
                subtitle = "",
                onClick = {
                    this.showHelp()
                }
            )

            UI.eventsRecycler.layoutManager = LinearLayoutManager(homeActivity)
            val userRamos :List<Ramo> = DataMaster.getUserRamos()
            UI.eventsRecycler.adapter = RamoEventsExpandedAdapter(data = userRamos)
            if (userRamos.count() == 0) {
                UI.eventsExportButton.isEnabled = false
            }
            UI.eventsExportButton.setOnClickListener {
                this.exportEvaluations()
            }
        }

        return fragView
    }

    /**
     * Converts the evaluations of inscribed ramos into a collection of calendar events and open
     * them with the default calendar app.
     */
    private fun exportEvaluations() {
        this.requireContext().yesNoDialog(
            title = "Exportar evaluaciones",
            message = "Las pruebas y exámenes de sus ramos tomados serán exportadas al calendario que escoja. ¿Continuar?",
            onYesClicked = {
                CalendarHandler.exportEventsToCalendar(activity = this.requireActivity())
            },
            onNoClicked = {},
            icon = null
        )
    }

    /**
     * Shows an information/help dialog about this view.
     */
    private fun showHelp() {
        this.requireContext().infoDialog(
            title = "Ayuda - Evaluaciones",
            message = """\
Aquí se ven las pruebas y exámenes de cada ramo que ud. haya tomado. \
Use el botón "Exportar al calendario" para indexar las evaluaciones en su calendario (de Google, \
por ejemplo).""".multilineTrim()
        )
    }
}