package com.ifgarces.tomaramosuandes

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.adapters.AgendaPortraitAdapter
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.ImageWorker
import com.ifgarces.tomaramosuandes.utils.Logf
import java.time.DayOfWeek


class AgendaPortraitFragment : Fragment() {

    companion object {
        /* Starts the fragment at the `caller` activity, at the view which ID matches `targetView` */
        public fun summon(caller :FragmentActivity, targetView :Int) {
            val transactioner :FragmentTransaction = caller.supportFragmentManager.beginTransaction()
                .replace(targetView, this.newInstance())
            transactioner.commit()
        }
        private fun newInstance() = AgendaPortraitFragment()
    }

    private object UI {
        lateinit var rootView         :View
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

        fun init(owner :View) {
            this.rootView         = owner
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

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        Logf("[AgendaPortraitFragment] Initializing...")
        UI.init( owner=inflater.inflate(R.layout.fragment_agenda_portrait, container, false) )

        val agendaEvents :Map<DayOfWeek, List<RamoEvent>> = DataMaster.getEventsByWeekDay()

        val dayRecyclers :Map<DayOfWeek, Pair<View, RecyclerView>> = mapOf(
            DayOfWeek.MONDAY    to UI.recyclerTeamMon,
            DayOfWeek.TUESDAY   to UI.recyclerTeamTue,
            DayOfWeek.WEDNESDAY to UI.recyclerTeamWed,
            DayOfWeek.THURSDAY  to UI.recyclerTeamThu,
            DayOfWeek.FRIDAY    to UI.recyclerTeamFri
        )
        dayRecyclers.forEach { (day :DayOfWeek, team :Pair<View, RecyclerView> ) -> // building agenda here
            team.second.layoutManager = LinearLayoutManager(this.context)
            team.second.adapter = AgendaPortraitAdapter(data=agendaEvents.getValue(day))
            if (agendaEvents.getValue(day).count() == 0) { // hiding the day if there is no event on it
                team.first.visibility = View.GONE
                team.second.visibility = View.GONE
            }
        }

        UI.saveAsImgButton.setColorFilter(Color.WHITE)
        UI.saveAsImgButton.setOnClickListener {
            Logf("[AgendaPortraitFragment] Exporting agenda as image...")
            ImageWorker.exportAgendaImage(
                context = this.context!!,
                targetView = UI.agendaScroll,
                largerView = UI.agendaLayout
            )
        }
        UI.fullScreenButton.setColorFilter(Color.WHITE)
        UI.fullScreenButton.setOnClickListener {
            UI.loadScreen.visibility = View.VISIBLE
            AgendaActivity.Companion.switchToLandscape = true
            AgendaLandscapeFragment.summon(
                caller = this.activity as FragmentActivity,
                targetView = R.id.agenda_fragmentContainer
            )
        }

        return UI.rootView
    }

    override fun onResume() {
        super.onResume()
        UI.loadScreen.visibility = View.GONE
    }
}