package com.ifgarces.tomaramosuandes

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.adapters.AgendaPortraitAdapter
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.ImageExporter
import com.ifgarces.tomaramosuandes.utils.Logf
import java.time.DayOfWeek


class AgendaPortraitActivity : AppCompatActivity() {

    private object UI {
        lateinit var loadScreen       :View
        lateinit var saveAsImgButton  :FloatingActionButton
        lateinit var fullScreenButton :FloatingActionButton
        lateinit var agendaScroll     :View // ScrollView
        lateinit var agendaLayout     :View // LinearLayout
        lateinit var recyclerTeamMon  :Pair<View, RecyclerView> // these hold the header (TextView) and their recycler attatched. They're a team.
        lateinit var recyclerTeamTue  :Pair<View, RecyclerView>
        lateinit var recyclerTeamWed  :Pair<View, RecyclerView>
        lateinit var recyclerTeamThu  :Pair<View, RecyclerView>
        lateinit var recyclerTeamFri  :Pair<View, RecyclerView>

        fun init(owner :AppCompatActivity) {
            this.loadScreen       = owner.findViewById(R.id.portrAgenda_loadScreen)
            this.saveAsImgButton  = owner.findViewById(R.id.portrAgenda_saveAsImage)
            this.fullScreenButton = owner.findViewById(R.id.portrAgenda_fullScreen)
            this.agendaScroll     = owner.findViewById(R.id.portrAgenda_scrollView)
            this.agendaLayout     = owner.findViewById(R.id.portrAgenda_layout)
            this.recyclerTeamMon  = Pair(
                owner.findViewById(R.id.portrAgenda_mondayHead),
                owner.findViewById(R.id.portrAgenda_mondayRecycler)
            )
            this.recyclerTeamTue = Pair(
                owner.findViewById(R.id.portrAgenda_tuesdayHead),
                owner.findViewById(R.id.portrAgenda_tuesdayRecycler)
            )
            this.recyclerTeamWed = Pair(
                owner.findViewById(R.id.portrAgenda_wednesdayHead),
                owner.findViewById(R.id.portrAgenda_wednesdayRecycler)
            )
            this.recyclerTeamThu = Pair(
                owner.findViewById(R.id.portrAgenda_thursdayHead),
                owner.findViewById(R.id.portrAgenda_thursdayRecycler)
            )
            this.recyclerTeamFri = Pair(
                owner.findViewById(R.id.portrAgenda_fridayHead),
                owner.findViewById(R.id.portrAgenda_fridayRecycler)
            )
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_agenda_portrait)
        UI.init(owner=this)

        UI.loadScreen.visibility = View.GONE

//        AsyncTask.execute {
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
//        }

        UI.saveAsImgButton.setColorFilter(Color.WHITE)
        UI.saveAsImgButton.setOnClickListener {
            Logf("[AgendaPortraitActivity] Exporting agenda as image...")
            ImageExporter.exportAgendaImage(
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