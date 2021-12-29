package com.ifgarces.tomaramosuandes.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.activities.ScheduleLandscapeActivity
import com.ifgarces.tomaramosuandes.adapters.SchedulePortraitAdapter
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.ImageExportHandler
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import java.time.DayOfWeek


/**
 * Fragment for displaying the week shcedule for the inscribed `Ramo`s in a portrait layout.
 */
class SchedulePortraitFragment : Fragment() {

    private class FragmentUI(owner :View) {
        val saveAsImgButton :FloatingActionButton =
            owner.findViewById(R.id.portrSchedule_saveAsImage)
        val fullScreenButton :FloatingActionButton =
            owner.findViewById(R.id.portrSchedule_fullScreen)
        val scheduleScrollView :ScrollView = owner.findViewById(R.id.portrSchedule_scrollView)
        val scheduleLayout :LinearLayout = owner.findViewById(R.id.portrSchedule_layout)
        val recyclerTeamMon :Pair<View, RecyclerView> =
            Pair( // these hold the header (TextView) and their recycler attatched. They're a team.
                owner.findViewById(R.id.portrSchedule_mondayHead),
                owner.findViewById(R.id.portrSchedule_mondayRecycler)
            )
        val recyclerTeamTue :Pair<View, RecyclerView> = Pair(
            owner.findViewById(R.id.portrSchedule_tuesdayHead),
            owner.findViewById(R.id.portrSchedule_tuesdayRecycler)
        )
        val recyclerTeamWed :Pair<View, RecyclerView> = Pair(
            owner.findViewById(R.id.portrSchedule_wednesdayHead),
            owner.findViewById(R.id.portrSchedule_wednesdayRecycler)
        )
        val recyclerTeamThu :Pair<View, RecyclerView> = Pair(
            owner.findViewById(R.id.portrSchedule_thursdayHead),
            owner.findViewById(R.id.portrSchedule_thursdayRecycler)
        )
        val recyclerTeamFri :Pair<View, RecyclerView> = Pair(
            owner.findViewById(R.id.portrSchedule_fridayHead),
            owner.findViewById(R.id.portrSchedule_fridayRecycler)
        )
    }

    private lateinit var UI :FragmentUI

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(
            if (DataMaster.getUserStats().nightModeOn) R.layout.night_fragment_schedule_portrait
            else R.layout.fragment_schedule_portrait,
            container, false
        )
        this.UI = FragmentUI(owner = fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            homeActivity.setBottomNavItemSelected(this::class)
            homeActivity.setTopToolbarValues(
                title = "Horario",
                subtitle = "",
                onClick = {
                    this.showHelp()
                }
            )

            homeActivity.hideLoadingScreen()

            homeActivity.runOnUiThread {
                // Initializing recyclers and hiding weekdays without events
                val agendaEvents :Map<DayOfWeek, List<RamoEvent>> = DataMaster.getEventsByWeekDay()
                mapOf(
                    DayOfWeek.MONDAY to UI.recyclerTeamMon,
                    DayOfWeek.TUESDAY to UI.recyclerTeamTue,
                    DayOfWeek.WEDNESDAY to UI.recyclerTeamWed,
                    DayOfWeek.THURSDAY to UI.recyclerTeamThu,
                    DayOfWeek.FRIDAY to UI.recyclerTeamFri
                ).forEach { (day :DayOfWeek, team :Pair<View, RecyclerView>) ->
                    team.second.layoutManager = LinearLayoutManager(homeActivity)
                    team.second.adapter = SchedulePortraitAdapter(data = agendaEvents.getValue(day))
                    if (agendaEvents.getValue(day).count() == 0) {
                        team.first.visibility = View.GONE
                        team.second.visibility = View.GONE
                    }
                }
            }

            UI.saveAsImgButton.setColorFilter(Color.WHITE)
            UI.saveAsImgButton.setOnClickListener {
                Logf.debug(this::class, "Exporting user schedule as image...")
                if (!ImageExportHandler.exportWeekScheduleImage(
                    activity = homeActivity,
                    targetView = UI.scheduleScrollView,
                    tallView = UI.scheduleLayout
                )) {
                    ImageExportHandler.showImageExportErrorDialog(homeActivity)
                }
            }
            UI.fullScreenButton.setColorFilter(Color.WHITE)
            UI.fullScreenButton.setOnClickListener {
                // As navigating to `ScheduleLandscapeActivity` is heavy as there are many `View`s
                // currently, we show a loading screen. We dismiss this loading screen `onResume`.
                homeActivity.showLoadingScreen()
                this.startActivity(
                    Intent(homeActivity, ScheduleLandscapeActivity::class.java)
                )
            }

            // Disabling floating actions when there is not a single `Ramo` to display
            if (DataMaster.getUserRamos().count() == 0) {
                listOf(UI.saveAsImgButton, UI.fullScreenButton).forEach { floatingButt :FloatingActionButton ->
                    floatingButt.isEnabled = false
                }
            }
        }

        return fragView
    }

    override fun onResume() {
        super.onResume()

        // The following is needed for when coming back from `ScheduleLandscapeActivity`
        (this.requireActivity() as HomeActivity).hideLoadingScreen()
    }

    private fun showHelp() {
        this.requireContext().infoDialog(
            title = "Ayuda - Horario",
            message = """\
En esta vista puede ver el horario semanal (clases, ayudantías y laboratorios) de sus ramos \
actualmente inscritos.""".multilineTrim(),
            onDismiss = {}
        )
    }
}