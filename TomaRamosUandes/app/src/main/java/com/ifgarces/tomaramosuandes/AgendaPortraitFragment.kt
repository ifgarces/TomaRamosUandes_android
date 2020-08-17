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

    public companion object {
        /* Starts the fragment at the `caller` activity, at the widget which ID matches `targetView` */
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
        lateinit var saveAsImgAction  :FloatingActionButton
        lateinit var fullScreenAction :FloatingActionButton
        lateinit var agendaScroll     :View // ScrollView
        lateinit var agendaLayout     :View // LinearLayout
        lateinit var dayHeaders       :List<View>
        lateinit var recyclerTeamMon  :Pair<View, RecyclerView> // these holds the header (TextView) and their recycler attatched. They're a team.
        lateinit var recyclerTeamTue  :Pair<View, RecyclerView>
        lateinit var recyclerTeamWed  :Pair<View, RecyclerView>
        lateinit var recyclerTeamThu  :Pair<View, RecyclerView>
        lateinit var recyclerTeamFri  :Pair<View, RecyclerView>

        fun init(owner :View) {
            this.rootView         = owner
            this.loadScreen       = owner.findViewById(R.id.portrAgenda_loadScreen)
            this.saveAsImgAction  = owner.findViewById(R.id.portrAgenda_saveAsImage)
            this.fullScreenAction = owner.findViewById(R.id.portrAgenda_fullScreen)
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
            this.dayHeaders = listOf(
                owner.findViewById(R.id.portrAgenda_mondayHead),
                owner.findViewById(R.id.portrAgenda_tuesdayHead),
                owner.findViewById(R.id.portrAgenda_wednesdayHead),
                owner.findViewById(R.id.portrAgenda_thursdayHead),
                owner.findViewById(R.id.portrAgenda_fridayHead)
            )
        }
    }

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        UI.init(owner=inflater.inflate(R.layout.fragment_agenda_portrait, container, false))

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
            team.second.adapter = agendaEvents[day]?.let { AgendaPortraitAdapter(data=it) } // <==> AgendaPortraitAdapter(agendaEvents[day]?)
            if (agendaEvents[day]?.count() == 0) { // hiding the day if there is no event on it
                team.first.visibility = View.GONE
                team.second.visibility = View.GONE
            }
        }

        UI.saveAsImgAction.setColorFilter(Color.WHITE)
        UI.saveAsImgAction.setOnClickListener {
            Logf("[AgendaPortraitFragment] Exporting agenda as image...")
            ImageWorker.exportAgendaImage(
                context = this.context!!,
                targetView = UI.agendaScroll,
                largerView = UI.agendaLayout
            )
        }
        UI.fullScreenAction.setColorFilter(Color.WHITE)
        UI.fullScreenAction.setOnClickListener {
            UI.loadScreen.visibility = View.VISIBLE
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