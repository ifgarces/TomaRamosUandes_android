package com.ifgarces.tomaramosuandes.fragments
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.AgendaLandscapeActivity
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.HomeActivity
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.adapters.AgendaPortraitAdapter
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.ImageExportHandler
import com.ifgarces.tomaramosuandes.utils.Logf
import java.time.DayOfWeek


/**
 * Fragment for displaying the week shcedule for the inscribed `Ramo`s in a portrait layout.
 */
class SchedulePortraitFragment : Fragment() {
    
    private class FragmentUI(owner :View) {
        val saveAsImgButton  :FloatingActionButton = owner.findViewById(R.id.portrAgenda_saveAsImage)
        val fullScreenButton :FloatingActionButton = owner.findViewById(R.id.portrAgenda_fullScreen)
        val agendaScroll     :View = owner.findViewById(R.id.portrAgenda_scrollView) // ScrollView
        val agendaLayout     :View = owner.findViewById(R.id.portrAgenda_layout) // LinearLayout
        val recyclerTeamMon  :Pair<View, RecyclerView> = Pair( // these hold the header (TextView) and their recycler attatched. They're a team.
            owner.findViewById(R.id.portrAgenda_mondayHead),
            owner.findViewById(R.id.portrAgenda_mondayRecycler)
        )
        val recyclerTeamTue  :Pair<View, RecyclerView> = Pair(
            owner.findViewById(R.id.portrAgenda_tuesdayHead),
            owner.findViewById(R.id.portrAgenda_tuesdayRecycler)
        )
        val recyclerTeamWed  :Pair<View, RecyclerView> = Pair(
            owner.findViewById(R.id.portrAgenda_wednesdayHead),
            owner.findViewById(R.id.portrAgenda_wednesdayRecycler)
        )
        val recyclerTeamThu  :Pair<View, RecyclerView> = Pair(
            owner.findViewById(R.id.portrAgenda_thursdayHead),
            owner.findViewById(R.id.portrAgenda_thursdayRecycler)
        )
        val recyclerTeamFri  :Pair<View, RecyclerView> = Pair(
            owner.findViewById(R.id.portrAgenda_fridayHead),
            owner.findViewById(R.id.portrAgenda_fridayRecycler)
        )
    }
    
    private lateinit var UI :FragmentUI

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(R.layout.fragment_schedule_portrait, container, false)
        this.UI = FragmentUI(owner=fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            homeActivity.hideLoadingScreen()

            homeActivity.runOnUiThread {
                /* initializing recyclers and hiding weekdays without events */
                val agendaEvents :Map<DayOfWeek, List<RamoEvent>> = DataMaster.getEventsByWeekDay()
                mapOf(
                    DayOfWeek.MONDAY    to UI.recyclerTeamMon,
                    DayOfWeek.TUESDAY   to UI.recyclerTeamTue,
                    DayOfWeek.WEDNESDAY to UI.recyclerTeamWed,
                    DayOfWeek.THURSDAY  to UI.recyclerTeamThu,
                    DayOfWeek.FRIDAY    to UI.recyclerTeamFri
                ).forEach { (day :DayOfWeek, team :Pair<View, RecyclerView> ) ->
                    team.second.layoutManager = LinearLayoutManager(homeActivity)
                    team.second.adapter = AgendaPortraitAdapter(data=agendaEvents.getValue(day))
                    if (agendaEvents.getValue(day).count() == 0) {
                        team.first.visibility = View.GONE
                        team.second.visibility = View.GONE
                    }
                }
            }

            UI.saveAsImgButton.setColorFilter(Color.WHITE)
            UI.saveAsImgButton.setOnClickListener {
                Logf(this::class, "[AgendaPortraitActivity] Exporting agenda as image...")
                ImageExportHandler.exportAgendaImage(
                    activity = homeActivity,
                    targetView = UI.agendaScroll,
                    tallView = UI.agendaLayout
                )
            }
            UI.fullScreenButton.setColorFilter(Color.WHITE)
            UI.fullScreenButton.setOnClickListener {
                homeActivity.showLoadingScreen()
                this.startActivity(
                    Intent(homeActivity, AgendaLandscapeActivity::class.java)
                )
            }
        }
        
        return fragView 
    }

    override fun onResume() {
        super.onResume()
        (this.requireActivity() as HomeActivity).hideLoadingScreen() //? needed?
    }
}