package com.ifgarces.tomaramosuandes.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.adapters.CareerAdvicesAdapter
import com.ifgarces.tomaramosuandes.adapters.IncomingRamoEventsAdapter
import com.ifgarces.tomaramosuandes.adapters.QuickHiperlinksAdapter
import com.ifgarces.tomaramosuandes.models.CareerAdvice
import com.ifgarces.tomaramosuandes.models.QuickHyperlink
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import com.ifgarces.tomaramosuandes.utils.toggleCollapseViewButton
import java.time.LocalDate
import java.time.temporal.ChronoUnit


/**
 * Home dashboard intended to be used during the semester, with links and useful stuff for students.
 */
class DashboardFragment : Fragment() {

    private class FragmentUI(owner :View) {
        // Incoming events section
        val incomingEventsHeadButton :MaterialButton = owner.findViewById(R.id.dashboard_eventsHeadButton)
        val incomingEventsRecycler :RecyclerView = owner.findViewById(R.id.dashboard_eventsRecycler)
        val incomingEventsContainer :View = owner.findViewById(R.id.dashboard_eventsContainer)

        // Useful links section
        val usefulLinksHeadButton :MaterialButton = owner.findViewById(R.id.dashboard_linksHeadButton)
        val usefulLinksRecycler :RecyclerView = owner.findViewById(R.id.dashboard_linksRecycler)
        val usefulLinksContainer :View = owner.findViewById(R.id.dashboard_linksContainer)

        // Advices section
        val careerAdvicesHeadButton :MaterialButton = owner.findViewById(R.id.dashboard_advicesHeadButton)
        val careerAdvicesRecycler :RecyclerView = owner.findViewById(R.id.dashboard_advicesRecycler)
        val careerAdvicesContainer :View = owner.findViewById(R.id.dashboard_advicesContainer)
    }

    private lateinit var UI :FragmentUI

    // Auxiliar variables for collapsing/expanding sections
    private var isEventsSectionCollapsed :Boolean = false
    private var isLinksSectionCollapsed :Boolean = false
    private var isAdvicesSectionCollapsed :Boolean = false

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(
            if (DataMaster.userStats.nightModeOn) R.layout.night_fragment_dashboard
            else R.layout.fragment_dashboard,
            container, false
        )
        this.UI = FragmentUI(owner=fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            homeActivity.setBottomNavItemSelected(this::class)
            homeActivity.setTopToolbarValues(
                title = "Inicio",
                subtitle = "Dashboard principal",
                onHelpClick = this::showHelp
            )

            // Applying stored user preferences on sections collapse/expanded status
            DataMaster.userStats.let {
                this.isEventsSectionCollapsed = it.dashboardEvalsSectionCollapsed
                this.isLinksSectionCollapsed = it.dashboardLinksSectionCollapsed
                this.isAdvicesSectionCollapsed = it.dashboardAdvicesSectionCollapsed
            }
            this.applyVisibilityUserSettings(homeActivity)

            UI.incomingEventsRecycler.layoutManager = LinearLayoutManager(
                homeActivity, LinearLayoutManager.HORIZONTAL, false
            )
            // Displaying events in the next 31 days
            UI.incomingEventsRecycler.adapter = IncomingRamoEventsAdapter(
                data = DataMaster.getUserEvaluations().map { event :RamoEvent ->
                    Pair(event, LocalDate.now().until(event.date!!, ChronoUnit.DAYS)) // Ref: https://discuss.kotlinlang.org/t/calculate-no-of-days-between-two-dates-kotlin/9826
                }.filter { (it.second <= 31L) && (it.second >= 0L) }
            )
            UI.usefulLinksRecycler.layoutManager = LinearLayoutManager(
                homeActivity, LinearLayoutManager.HORIZONTAL, false
            )
            UI.usefulLinksRecycler.adapter = QuickHiperlinksAdapter(
                data = QuickHyperlink.getStaticQuickLinks(context = homeActivity),
                activity = homeActivity
            )
            UI.careerAdvicesRecycler.layoutManager = LinearLayoutManager(
                homeActivity, LinearLayoutManager.VERTICAL, false
            )
            UI.careerAdvicesRecycler.adapter = CareerAdvicesAdapter(
                rawData = CareerAdvice.getStaticAdvices(context = homeActivity),
                activity = homeActivity
            )

            // Setting on-click toggle events for sections (not easy to avoid boilerplate code!)
            UI.incomingEventsHeadButton.setOnClickListener {
                homeActivity.toggleCollapseViewButton(
                    isCollapsed = this.isEventsSectionCollapsed,
                    toggleButton = UI.incomingEventsHeadButton,
                    targetContainer = UI.incomingEventsContainer
                )
                this.isEventsSectionCollapsed = !this.isEventsSectionCollapsed
                DataMaster.toggleSectionCollapsed(RamoEvent::class)
            }
            UI.usefulLinksHeadButton.setOnClickListener {
                homeActivity.toggleCollapseViewButton(
                    isCollapsed = this.isLinksSectionCollapsed,
                    toggleButton = UI.usefulLinksHeadButton,
                    targetContainer = UI.usefulLinksContainer
                )
                this.isLinksSectionCollapsed = !this.isLinksSectionCollapsed
                DataMaster.toggleSectionCollapsed(QuickHyperlink::class)
            }
            UI.careerAdvicesHeadButton.setOnClickListener {
                homeActivity.toggleCollapseViewButton(
                    isCollapsed = this.isAdvicesSectionCollapsed,
                    toggleButton = UI.careerAdvicesHeadButton,
                    targetContainer = UI.careerAdvicesContainer
                )
                this.isAdvicesSectionCollapsed = !this.isAdvicesSectionCollapsed
                DataMaster.toggleSectionCollapsed(CareerAdvice::class)
            }
        }

        return fragView
    }

    /**
     * Sets visibility for sections according to the data stored in `UserStats` table (Room DB).
     */
    private fun applyVisibilityUserSettings(context :Context) {
        listOf(
            Triple(this.isEventsSectionCollapsed, UI.incomingEventsHeadButton, UI.incomingEventsContainer),
            Triple(this.isLinksSectionCollapsed, UI.usefulLinksHeadButton, UI.usefulLinksContainer),
            Triple(this.isAdvicesSectionCollapsed, UI.careerAdvicesHeadButton, UI.careerAdvicesContainer)
        ).forEach { (collapsed :Boolean, toggleButton :MaterialButton, container :View) ->
            container.visibility = if (collapsed) View.GONE else View.VISIBLE
            toggleButton.icon = ContextCompat.getDrawable(
                context, if (collapsed) R.drawable.arrow_tip_right else R.drawable.arrow_tip_down
            )
        }
    }

    private fun showHelp() {
        this.requireContext().infoDialog(
            title = "Ayuda — Inicio",
            message = """\
Este es el tablero principal donde aparecen secciones útiles, como las evaluaciones cercanas, \
links útiles y consejos varios. Puede hacer clic en algún consejo expandido para ir al link del que \
hace referencia.\
""".multilineTrim(),
            icon = R.drawable.help_icon
        )
    }
}
