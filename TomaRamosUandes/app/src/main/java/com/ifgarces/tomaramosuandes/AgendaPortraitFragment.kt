package com.ifgarces.tomaramosuandes

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.adapters.RamoEventCardsAdapter
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.Logf
import java.time.DayOfWeek


class AgendaPortraitFragment : Fragment() {

    public companion object {
        public fun summon(caller :FragmentActivity, widget_id :Int) {
            val transactioner :FragmentTransaction = caller.supportFragmentManager.beginTransaction()
                .replace(widget_id, this.newInstance())
            transactioner.commit()
        }
        private fun newInstance() = AgendaPortraitFragment()
    }

    private object UI {
        lateinit var rootView         :View
        lateinit var fullScreenAction :FloatingActionButton
        lateinit var dayHeaders       :List<TextView>
        lateinit var recyclerMon      :RecyclerView
        lateinit var recyclerTue      :RecyclerView
        lateinit var recyclerWed      :RecyclerView
        lateinit var recyclerThu      :RecyclerView
        lateinit var recyclerFri      :RecyclerView

        fun init(owner :View) {
            this.rootView = owner
            this.fullScreenAction = owner.findViewById(R.id.portrAgenda_fullScreen)
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

    private object DayIndexes {
        const val MON :Int = 0 // lists indexes (recyclers and headers day markers)
        const val TUE :Int = 1
        const val WED :Int = 2
        const val THU :Int = 3
        const val FRI :Int = 4
    }

    private object EventType {
        const val CLAS :Int = 0
        const val AYUD :Int = 1
        const val LABT :Int = 2
        const val PRBA :Int = 3
    }


    private val recyclerData = object {
        val monday    :MutableList<RamoEvent> = mutableListOf()
        val tuesday   :MutableList<RamoEvent> = mutableListOf()
        val wednesday :MutableList<RamoEvent> = mutableListOf()
        val thursday  :MutableList<RamoEvent> = mutableListOf()
        val friday    :MutableList<RamoEvent> = mutableListOf()
    }

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        UI.init(owner=inflater.inflate(R.layout.fragment_agenda_portrait, container, false))

        val agendaEvents :Map<DayOfWeek, List<RamoEvent>> = DataMaster.getAgendaEvents()

        UI.recyclerMon.layoutManager = LinearLayoutManager(this.context)
        UI.recyclerMon.adapter = RamoEventCardsAdapter(data=agendaEvents[DayOfWeek.MONDAY])
        UI.recyclerTue.layoutManager = LinearLayoutManager(this.context)
        UI.recyclerMon.adapter = RamoEventCardsAdapter(data=agendaEvents[DayOfWeek.TUESDAY])
        // ... TODO: finish

//        for (recycler :RecyclerView in UI.dayRecyclers) {
//            recycler.layoutManager = LinearLayoutManager(this.context)
//            recycler.adapter = RamoEventCardsAdapter(data=DataMaster.getAgendaEvents())
//        }

        UI.fullScreenAction.setColorFilter(Color.WHITE)
        UI.fullScreenAction.setOnClickListener {
            AgendaLandscapeFragment.summon(
                caller=this.activity as FragmentActivity, // <- [!] Will crash?
                widget_id=R.id.agenda_fragmentContainer
            )
        }

        this.buildAgenda(data=DataMaster.getUserRamos())
        return UI.rootView
    }

    private fun buildAgenda(data :List<Ramo>) {
        for (ramo :Ramo in data) {
            for (event :RamoEvent in ramo.events) {
                when(event.dayofWeek) {
                    DayOfWeek.MONDAY    -> this.recyclerData.monday.add(event)
                    DayOfWeek.TUESDAY   -> this.recyclerData.tuesday.add(event)
                    DayOfWeek.WEDNESDAY -> this.recyclerData.wednesday.add(event)
                    DayOfWeek.THURSDAY  -> this.recyclerData.thursday.add(event)
                    DayOfWeek.FRIDAY    -> this.recyclerData.friday.add(event)
                    else -> Logf("[AgendaPortraitFragment] Warning: agenda event on weekend: %s", event)
                }
            }
        }
    }

    private fun blockClick(sender :Button) {
        // TODO: fill
    }

    private fun exportImage() {
        // TODO: fill
    }
}