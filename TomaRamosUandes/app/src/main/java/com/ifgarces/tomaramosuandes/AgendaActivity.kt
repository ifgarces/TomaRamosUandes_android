package com.ifgarces.tomaramosuandes

import android.os.Bundle
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class AgendaActivity : AppCompatActivity() {

    private lateinit var fragSwitcher :FragmentTransaction

    private object PortraitUI { // portrait orientation widgets
        lateinit var fullScreenAction :FloatingActionButton
        lateinit var dayRecyclers     :List<RecyclerView>
        lateinit var dayHeaders       :List<TextView>
        val MON :Int = 0 // lists indexes (recyclers and headers day markers)
        val TUE :Int = 1
        val WED :Int = 2
        val THU :Int = 3
        val FRI :Int = 4

        fun init(owner :AppCompatActivity) {
            this.fullScreenAction = owner.findViewById(R.id.agendaP_fullScreen)
            this.dayRecyclers = listOf(
                owner.findViewById(R.id.agendaP_mondayRecycler),
                owner.findViewById(R.id.agendaP_tuesdayRecycler),
                owner.findViewById(R.id.agendaP_wednesdayRecycler),
                owner.findViewById(R.id.agendaP_thursdayRecycler),
                owner.findViewById(R.id.agendaP_fridayRecycler)
            )
            this.dayHeaders = listOf(
                owner.findViewById(R.id.agendaP_mondayHead),
                owner.findViewById(R.id.agendaP_tuesdayHead),
                owner.findViewById(R.id.agendaP_wednesdayHead),
                owner.findViewById(R.id.agendaP_thursdayHead),
                owner.findViewById(R.id.agendaP_fridayHead)
            )
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_agenda)
    }

    override fun onResume() {
        super.onResume()

        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.showPortraitAgenda()
        }
        else {
            this.showLandscapeAgenda()
        }
    }

    private fun showPortraitAgenda() {

    }

    private fun showLandscapeAgenda() {
        this.fragSwitcher = this.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AgendaLandscapeFragment())
        fragSwitcher.commit()
    }

    private fun buildAgenda() {
        // TODO: fill
    }

    private fun blockClick(sender :Button) {
        if (sender.text == "") return
        // TODO: handle agenda element click. Show block details (may contain multiple Ramo events, on conflict)
    }
}