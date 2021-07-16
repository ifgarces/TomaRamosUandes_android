package com.ifgarces.tomaramosuandes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.adapters.AgendaPortraitAdapter
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.ImageExportHandler
import com.ifgarces.tomaramosuandes.utils.Logf
import java.time.DayOfWeek


class AgendaPortraitActivity : AppCompatActivity() {

    private class ActivityUI(owner :AppCompatActivity) {
        val loadScreen       :View = owner.findViewById(R.id.portrAgenda_loadScreen)
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
    private lateinit var UI :ActivityUI

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_agenda_portrait)
        this.UI = ActivityUI(owner=this)

        UI.loadScreen.visibility = View.GONE

        val agendaEvents :Map<DayOfWeek, List<RamoEvent>> = DataMaster.getEventsByWeekDay()

        this.runOnUiThread {
            /* initializing recyclers and hiding weekdays without events */
            mapOf(
                DayOfWeek.MONDAY    to UI.recyclerTeamMon,
                DayOfWeek.TUESDAY   to UI.recyclerTeamTue,
                DayOfWeek.WEDNESDAY to UI.recyclerTeamWed,
                DayOfWeek.THURSDAY  to UI.recyclerTeamThu,
                DayOfWeek.FRIDAY    to UI.recyclerTeamFri
            ).forEach { (day :DayOfWeek, team :Pair<View, RecyclerView> ) ->
                team.second.layoutManager = LinearLayoutManager(this)
                team.second.adapter = AgendaPortraitAdapter(data=agendaEvents.getValue(day))
                if (agendaEvents.getValue(day).count() == 0) {
                    team.first.visibility = View.GONE
                    team.second.visibility = View.GONE
                }
            }
        }

        UI.saveAsImgButton.setColorFilter(Color.WHITE)
        UI.saveAsImgButton.setOnClickListener {
            Logf("[AgendaPortraitActivity] Exporting agenda as image...")
            ImageExportHandler.exportAgendaImage(
                activity = this,
                targetView = UI.agendaScroll,
                tallView = UI.agendaLayout
            )
        }
        UI.fullScreenButton.setColorFilter(Color.WHITE)
        UI.fullScreenButton.setOnClickListener {
            UI.loadScreen.visibility = View.VISIBLE
            this.startActivity(
                Intent(this, AgendaLandscapeActivity::class.java)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        UI.loadScreen.visibility = View.GONE
    }
}