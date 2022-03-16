package com.ifgarces.tomaramosuandes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.adapters.IncomingRamoEventsAdapter
import com.ifgarces.tomaramosuandes.adapters.WebLinksAdapter
import com.ifgarces.tomaramosuandes.models.PrettyHyperlink
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.toastf


class DashboardFragment : Fragment() {

    private class FragmentUI(owner :View) {
        // Incoming events section
        val incomingEventsHeadBtn :MaterialButton = owner.findViewById(R.id.dashboard_incomingEventsHeadBtn)
        val incomingEventsContainer :View = owner.findViewById(R.id.dashboard_incomingEventsContainer)
        val incomingEventsRecycler :RecyclerView = owner.findViewById(R.id.dashboard_incomingEventsRecycler)

        // Useful links section
        val usefulLinksHeadBtn :MaterialButton = owner.findViewById(R.id.dashboard_linksHeadBtn)
        val usefulLinksContainer :View = owner.findViewById(R.id.dashboard_linksContainer)
        val usefulLinksRecycler :RecyclerView = owner.findViewById(R.id.dashboard_linksRecycler)

        // Advices section
        //TODO
    }

    private lateinit var UI :FragmentUI

    // Auxiliar variables for collapsing/expanding sections
    private var isEventsSectionCollapsed :Boolean = false
    private var isLinksSectionCollapsed :Boolean = false
    private var isAdvicesSectionCollapsed :Boolean = false

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(R.layout.fragment_dashboard, container, false)
        this.UI = FragmentUI(owner=fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            homeActivity.setBottomNavItemSelected(this::class)
            homeActivity.setTopToolbarValues(
                title = "Inicio",
                subtitle = "Dashboard principal",
                onHelpClick = {} //TODO: show help
            )

            // Setting up recyclers
            UI.incomingEventsRecycler.layoutManager = LinearLayoutManager(
                homeActivity, LinearLayoutManager.HORIZONTAL, false
            )
            UI.incomingEventsRecycler.adapter = IncomingRamoEventsAdapter(
                data = DataMaster.getUserEvaluations() //TODO: only show events in the next 31 days
            )
            UI.usefulLinksRecycler.layoutManager = GridLayoutManager(
                homeActivity, 2, LinearLayoutManager.HORIZONTAL, false
            )
            UI.usefulLinksRecycler.adapter = WebLinksAdapter(
                data = listOf( // hardcoded because this is not supposed to change. Is this the way to do this?
                    PrettyHyperlink(
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_canvas)!!,
                        name = "Canvas",
                        uri = "https://uandes.instructure.com"
                    ),
                    PrettyHyperlink(
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_banner)!!,
                        name = "Banner MiUandes",
                        uri = "https://mi.uandes.cl/PROD/twbkwbis.P_WWWLogin"
                    ),
                    PrettyHyperlink(
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.uandes_logo_simple)!!,
                        name = "SAF",
                        uri = "https://saf.uandes.cl/ing"
                    )
                ),
                activity = this.requireActivity() as HomeActivity
            )

            //TODO: properly show the next events, e.g. the next 2 events this month, etc.
            //TODO: SHOW RELATIVE TIME (e.g. "mañana", "en 6 días", etc.)

            UI.incomingEventsHeadBtn.setOnClickListener {
                this.onCollapseToggleButton(
                    isCollapsed = this.isEventsSectionCollapsed,
                    sectionButton = UI.incomingEventsHeadBtn,
                    sectionContainer = UI.incomingEventsContainer
                )
                this.isEventsSectionCollapsed = !this.isEventsSectionCollapsed
            }

            UI.usefulLinksHeadBtn.setOnClickListener {
                this.onCollapseToggleButton(
                    isCollapsed = this.isLinksSectionCollapsed,
                    sectionButton = UI.usefulLinksHeadBtn,
                    sectionContainer = UI.usefulLinksContainer
                )
                this.isLinksSectionCollapsed = !this.isLinksSectionCollapsed
            }
        }

        return fragView
    }

    /**
     * On-click behaviour for the buttons of the headers for collapsing/expanding sections, as well
     * as for updating the button icon.
     * @param isCollapsed Wether the current section is collapsed or not right when the user clicks
     * the collapse/expand toggle button.
     * @param sectionButton Button for collapsing/expanding the section.
     * @param sectionContainer Container for the elements of the section.
     */
    private fun onCollapseToggleButton(
        isCollapsed :Boolean, sectionButton :MaterialButton, sectionContainer :View
    ) {
        sectionContainer.visibility = if (isCollapsed) View.VISIBLE else View.GONE
        //TODO: add basic animations
        sectionButton.icon = ContextCompat.getDrawable(
            this.requireContext(),
            if (isCollapsed) R.drawable.arrow_tip_down else R.drawable.arrow_tip_right
        )
    }
}
