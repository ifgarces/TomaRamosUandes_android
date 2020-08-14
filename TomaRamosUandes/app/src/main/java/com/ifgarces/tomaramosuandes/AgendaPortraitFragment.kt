package com.ifgarces.tomaramosuandes

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.adapters.RamoEventCardsAdapter
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.ImageWorker
import com.ifgarces.tomaramosuandes.utils.Logf
import java.time.DayOfWeek


class AgendaPortraitFragment : Fragment() {

    public companion object {
        /* Starts the fragment at the `caller` activity, at the widget which ID matches `targetView` */
        public fun showNow(caller :FragmentActivity, targetView :Int) {
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
        lateinit var dayHeaders       :List<TextView>
        lateinit var recyclerMon      :RecyclerView
        lateinit var recyclerTue      :RecyclerView
        lateinit var recyclerWed      :RecyclerView
        lateinit var recyclerThu      :RecyclerView
        lateinit var recyclerFri      :RecyclerView

        fun init(owner :View) {
            this.rootView         = owner
            this.loadScreen       = owner.findViewById(R.id.portrAgenda_loadScreen)
            this.saveAsImgAction  = owner.findViewById(R.id.portrAgenda_saveAsImage)
            this.fullScreenAction = owner.findViewById(R.id.portrAgenda_fullScreen)
            this.agendaScroll     = owner.findViewById(R.id.portrAgenda_scrollView)
            this.agendaLayout     = owner.findViewById(R.id.portrAgenda_layout)
            this.recyclerMon      = owner.findViewById(R.id.portrAgenda_mondayRecycler)
            this.recyclerTue      = owner.findViewById(R.id.portrAgenda_tuesdayRecycler)
            this.recyclerWed      = owner.findViewById(R.id.portrAgenda_wednesdayRecycler)
            this.recyclerThu      = owner.findViewById(R.id.portrAgenda_thursdayRecycler)
            this.recyclerFri      = owner.findViewById(R.id.portrAgenda_fridayRecycler)
            this.dayHeaders = listOf(
                owner.findViewById(R.id.portrAgenda_mondayHead),
                owner.findViewById(R.id.portrAgenda_tuesdayHead),
                owner.findViewById(R.id.portrAgenda_wednesdayHead),
                owner.findViewById(R.id.portrAgenda_thursdayHead),
                owner.findViewById(R.id.portrAgenda_fridayHead)
            )
        }
    }

    lateinit var dayRecyclers :Map<DayOfWeek, RecyclerView>

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        UI.init(owner=inflater.inflate(R.layout.fragment_agenda_portrait, container, false))

        val agendaEvents :Map<DayOfWeek, List<RamoEvent>> = DataMaster.getEventsByDay()

        this.dayRecyclers = mapOf(
            DayOfWeek.MONDAY to UI.recyclerMon,
            DayOfWeek.TUESDAY to UI.recyclerTue,
            DayOfWeek.WEDNESDAY to UI.recyclerWed,
            DayOfWeek.THURSDAY to UI.recyclerThu,
            DayOfWeek.FRIDAY to UI.recyclerFri
        )
        this.dayRecyclers.forEach { (day :DayOfWeek, recycler :RecyclerView) -> // building agenda here
            recycler.layoutManager = LinearLayoutManager(this.context)
            recycler.adapter = agendaEvents[day]?.let { RamoEventCardsAdapter(data=it) }
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
            AgendaLandscapeFragment.showNow(
                caller=this.activity as FragmentActivity,
                targetView=R.id.agenda_fragmentContainer
            )
        }

        return UI.rootView
    }

    override fun onResume() {
        super.onResume()
        UI.loadScreen.visibility = View.GONE
    }
}