package com.ifgarces.tomaramosuandes

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import java.time.LocalTime
import com.ifgarces.tomaramosuandes.utils.ImageWorker
import com.ifgarces.tomaramosuandes.utils.Logf
import java.time.DayOfWeek


class AgendaLandscapeFragment : Fragment() {

    public companion object {
        /* Starts the fragment at the `caller` activity, at the widget which ID matches `targetView` */
        public fun summon(caller :FragmentActivity, targetView :Int) {
            val transactioner :FragmentTransaction = caller.supportFragmentManager.beginTransaction()
                .replace(targetView, this.newInstance())
            transactioner.commit()
        }
        private fun newInstance() = AgendaLandscapeFragment()
    }

    private object UI {
        lateinit var rootView         :View
        lateinit var saveAsImgAction  :FloatingActionButton
        lateinit var agendaBodyScroll :View // ScrollView
        lateinit var agendaBodyLayout :View // LinearLayout
        lateinit var blocksMap        :Map<DayOfWeek, List<Button>>

        fun init(owner :View) {
            this.rootView = owner
            this.saveAsImgAction = owner.findViewById(R.id.landAgenda_saveAsImage)
            this.agendaBodyScroll = owner.findViewById(R.id.landAgenda_bodyScrollView)
            this.agendaBodyLayout = owner.findViewById(R.id.landAgenda_bodyLayout)
            this.blocksMap = mapOf(
                DayOfWeek.MONDAY to listOf(
                    owner.findViewById(R.id.landAgenda_lun0),
                    owner.findViewById(R.id.landAgenda_lun1),
                    owner.findViewById(R.id.landAgenda_lun2),
                    owner.findViewById(R.id.landAgenda_lun3),
                    owner.findViewById(R.id.landAgenda_lun4),
                    owner.findViewById(R.id.landAgenda_lun5),
                    owner.findViewById(R.id.landAgenda_lun6),
                    owner.findViewById(R.id.landAgenda_lun7),
                    owner.findViewById(R.id.landAgenda_lun8),
                    owner.findViewById(R.id.landAgenda_lun9),
                    owner.findViewById(R.id.landAgenda_lun10),
                    owner.findViewById(R.id.landAgenda_lun11),
                    owner.findViewById(R.id.landAgenda_lun12),
                    owner.findViewById(R.id.landAgenda_lun13)
                ),
                DayOfWeek.TUESDAY to listOf(
                    owner.findViewById(R.id.landAgenda_mar0),
                    owner.findViewById(R.id.landAgenda_mar1),
                    owner.findViewById(R.id.landAgenda_mar2),
                    owner.findViewById(R.id.landAgenda_mar3),
                    owner.findViewById(R.id.landAgenda_mar4),
                    owner.findViewById(R.id.landAgenda_mar5),
                    owner.findViewById(R.id.landAgenda_mar6),
                    owner.findViewById(R.id.landAgenda_mar7),
                    owner.findViewById(R.id.landAgenda_mar8),
                    owner.findViewById(R.id.landAgenda_mar9),
                    owner.findViewById(R.id.landAgenda_mar10),
                    owner.findViewById(R.id.landAgenda_mar11),
                    owner.findViewById(R.id.landAgenda_mar12),
                    owner.findViewById(R.id.landAgenda_mar13)
                ),
                DayOfWeek.WEDNESDAY to listOf(
                    owner.findViewById(R.id.landAgenda_mie0),
                    owner.findViewById(R.id.landAgenda_mie1),
                    owner.findViewById(R.id.landAgenda_mie2),
                    owner.findViewById(R.id.landAgenda_mie3),
                    owner.findViewById(R.id.landAgenda_mie4),
                    owner.findViewById(R.id.landAgenda_mie5),
                    owner.findViewById(R.id.landAgenda_mie6),
                    owner.findViewById(R.id.landAgenda_mie7),
                    owner.findViewById(R.id.landAgenda_mie8),
                    owner.findViewById(R.id.landAgenda_mie9),
                    owner.findViewById(R.id.landAgenda_mie10),
                    owner.findViewById(R.id.landAgenda_mie11),
                    owner.findViewById(R.id.landAgenda_mie12),
                    owner.findViewById(R.id.landAgenda_mie13)
                ),
                DayOfWeek.THURSDAY to listOf(
                    owner.findViewById(R.id.landAgenda_jue0),
                    owner.findViewById(R.id.landAgenda_jue1),
                    owner.findViewById(R.id.landAgenda_jue2),
                    owner.findViewById(R.id.landAgenda_jue3),
                    owner.findViewById(R.id.landAgenda_jue4),
                    owner.findViewById(R.id.landAgenda_jue5),
                    owner.findViewById(R.id.landAgenda_jue6),
                    owner.findViewById(R.id.landAgenda_jue7),
                    owner.findViewById(R.id.landAgenda_jue8),
                    owner.findViewById(R.id.landAgenda_jue9),
                    owner.findViewById(R.id.landAgenda_jue10),
                    owner.findViewById(R.id.landAgenda_jue11),
                    owner.findViewById(R.id.landAgenda_jue12),
                    owner.findViewById(R.id.landAgenda_jue13)
                ),
                DayOfWeek.FRIDAY to listOf(
                    owner.findViewById(R.id.landAgenda_vie0),
                    owner.findViewById(R.id.landAgenda_vie1),
                    owner.findViewById(R.id.landAgenda_vie2),
                    owner.findViewById(R.id.landAgenda_vie3),
                    owner.findViewById(R.id.landAgenda_vie4),
                    owner.findViewById(R.id.landAgenda_vie5),
                    owner.findViewById(R.id.landAgenda_vie6),
                    owner.findViewById(R.id.landAgenda_vie7),
                    owner.findViewById(R.id.landAgenda_vie8),
                    owner.findViewById(R.id.landAgenda_vie9),
                    owner.findViewById(R.id.landAgenda_vie10),
                    owner.findViewById(R.id.landAgenda_vie11),
                    owner.findViewById(R.id.landAgenda_vie12),
                    owner.findViewById(R.id.landAgenda_vie13)
                )
            )
        }
    }

    val ONSCROLL_BUTTON_RESPAWN_TIME :Long = 1500 // time passed between the FloatingActionButton dissapears due scrolling and it appears again
    val ROWS_COUNT :Int = 14 // number of rows of agenda i.e. number of blocks per day of week

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        UI.init(owner=inflater.inflate(R.layout.fragment_agenda_landscape, container, false))

        UI.saveAsImgAction.setColorFilter(Color.WHITE)

        //UI.rootView.postDelayed({ /*initialize things*/ }, 0)
        this.activity!!.runOnUiThread { // <- will work??
            UI.saveAsImgAction.setOnClickListener {
                Logf("[AgendaLandscapeFragment] Exporting agenda as image...")
                ImageWorker.exportAgendaImage(
                    context = this.context!!,
                    targetView = UI.agendaBodyScroll,
                    largerView = UI.agendaBodyLayout
                )
            }
            for (row :Int in (0 until this.ROWS_COUNT)) {
                for (day :DayOfWeek in listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    (UI.blocksMap[day]?.get(row))!!.text = ""
                    // TODO: handle OnClick event
                }
            }

            /* Hiding the floating button when user scrolls the agenda and showing it after some time */
            UI.agendaBodyScroll.setOnScrollChangeListener { _ :View, _ :Int, _ :Int, _ :Int, _ :Int ->
                if (UI.saveAsImgAction.visibility == View.VISIBLE) {
                    UI.saveAsImgAction.visibility = View.GONE
                    UI.saveAsImgAction.postDelayed({
                        UI.saveAsImgAction.visibility = View.VISIBLE
                    }, this.ONSCROLL_BUTTON_RESPAWN_TIME)
                }
            }

            AgendaWorker.buildAgenda(context=this.context!!)
        }

        this.enterFullScreen()
        return UI.rootView
    }

    /* Enters 'inmersive mode', hiding system LandscapeUI elements (and forcing landscape orientation) */
    private fun enterFullScreen() { /// references: https://developer.android.com/training/system-ui/immersive.html
        this.activity!!.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
        this.activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    private object AgendaWorker {
        private val agendaData :MutableMap<Button, MutableList<RamoEvent>> = mutableMapOf()

        public fun blockClick(sender :Button) {
            val events :MutableList<RamoEvent>? = agendaData[sender]
        }

        /* Displays the non-evaluation events for each user taken `Ramo` in the agenda */
        public fun buildAgenda(context :Context) {
            Logf("[AgendaLandscapeFragment] Building agenda...")

            /* initialization start --- */
            this.agendaData.clear()
            UI.blocksMap.forEach { (_ :DayOfWeek, buttons :List<Button>) ->
                buttons.forEach {
                    this.agendaData[it] = mutableListOf()
                }
            }
            /* --- initialization end */

            /* filling `agendaData` i.e. mapping agenda block buttons with corresponding event(s) */
            var rowInterval :Pair<Int, Int> // ~ (rowStart, rowEnd)
            DataMaster.getEventsByWeekDay().forEach { (day :DayOfWeek, events :List<RamoEvent>) ->
                for (ev :RamoEvent in events) {
                    rowInterval = timeInterval_toBlockRow(start=ev.startTime, end=ev.endTime)!!
                    for (rowNum :Int in (rowInterval.first until rowInterval.second)) {
                        this.agendaData[ UI.blocksMap[day]?.get(rowNum)!! ]?.add(ev) // <==> agendaData[ UI.blocksMap[day][rowNum]!! ].add(ev)
                    }
                }
            }

            /* finally, displaying events assigned to each block button */
            this.agendaData.forEach { (block :Button, events :MutableList<RamoEvent>) ->
                events.forEachIndexed { index :Int, event :RamoEvent ->
                    if (index == 0) {
                        block.text = DataMaster.findRamo(NRC=event.ramoNRC, searchInUserList=true)!!.nombre
                        // setting background color according to eventType
                        val backColor :Int? = when(event.type) {
                            RamoEventType.CLAS -> { context.getColor(android.R.color.transparent) }
                            RamoEventType.AYUD -> { context.getColor(R.color.ayud) }
                            RamoEventType.LABT, RamoEventType.TUTR -> { context.getColor(R.color.labt) }
                            else -> { null } // <- will never happen, unless dumb code mistake
                        }
                        block.setBackgroundColor(backColor!!)
                    }
                    else {
                        block.setBackgroundColor( context.getColor(R.color.conflict_background) ) // visibly marking block as conflicted
                        // concatenating multiple events in same block
                        block.text = "%s + %s".format(block.text, DataMaster.findRamo(NRC=event.ramoNRC, searchInUserList=true)!!.nombre)
                        if (block.text.toString().filter{ it == '+' }.count() >= 1) { // if three or more events are in the same block, avoid showing all of them. They all can be viewed when on click.
                            block.text = "%s + ...".format(block.text)
                            return@forEach
                        }
                    }
                }
            }

            Logf("[AgendaLandscapeFragment] Agenda successfully built")
        }

        private fun timeInterval_toBlockRow(start :LocalTime, end :LocalTime) : Pair<Int, Int>? {
            val supportedHours_start :List<Int> = (8 until 21+1).toList() // [8, 21]
            val supportedHours_end :List<Int> = (9 until 22).toList() // [9, 22]

            var rowStart :Int = supportedHours_start.indexOf(start.hour)
            var rowEnd   :Int = supportedHours_end.indexOf(end.hour)
            if (rowStart == -1 || rowEnd == -1) { return null } // invalid hour(s): block wouldn't fit in agenda
            if (start.minute > 30) { rowStart+=1 }
            if (end.minute > 20) { rowEnd+=1 }

            return Pair(rowStart, rowEnd)
        }
    }


}