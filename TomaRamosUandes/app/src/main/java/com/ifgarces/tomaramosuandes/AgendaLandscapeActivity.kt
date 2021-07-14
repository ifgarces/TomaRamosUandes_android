package com.ifgarces.tomaramosuandes

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import com.ifgarces.tomaramosuandes.utils.*
import java.time.DayOfWeek
import java.time.LocalTime


/* Activity forced to be landsape (in manifest) */
class AgendaLandscapeActivity : AppCompatActivity() {

    private val ONSCROLL_BUTTON_RESPAWN_TIME :Long = 1500 // time passed between the FloatingActionButton dissapears due scrolling and it appears again (milliseconds)
    private val ROWS_COUNT :Int = 14 // number of rows of agenda i.e. number of blocks per day of week

    private object UI {
        lateinit var saveAsImgButton        :FloatingActionButton
        lateinit var toggleFullScreenButton :FloatingActionButton
        lateinit var agendaBodyScroll       :View // ScrollView
        lateinit var agendaBodyLayout       :View // LinearLayout
        lateinit var blocksMap              :Map<DayOfWeek, List<Button>>

        fun init(owner :AppCompatActivity) {
            this.saveAsImgButton        = owner.findViewById(R.id.landAgenda_saveAsImage)
            this.toggleFullScreenButton = owner.findViewById(R.id.landAgenda_toggleFullScreen)
            this.agendaBodyScroll       = owner.findViewById(R.id.landAgenda_bodyScrollView)
            this.agendaBodyLayout       = owner.findViewById(R.id.landAgenda_bodyLayout)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_agenda_landscape)
        UI.init(owner=this)

        UI.saveAsImgButton.setColorFilter(Color.WHITE)
        UI.saveAsImgButton.setOnClickListener {
            Logf("[AgendaLandscapeActivity] Exporting agenda as image...")
            ImageExportHandler.exportAgendaImage(
                activity = this,
                targetView = UI.agendaBodyScroll,
                tallView = UI.agendaBodyLayout
            )
        }
        UI.toggleFullScreenButton.setColorFilter(Color.WHITE)
        UI.toggleFullScreenButton.setOnClickListener {
            this.toggleFullScreen()
        }
        for (row :Int in (0 until this.ROWS_COUNT)) {
            for (day :DayOfWeek in listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )) {
                UI.blocksMap.getValue(key=day)[row].text = ""
                UI.blocksMap.getValue(key=day)[row].setOnClickListener {
                    AgendaWorker.onBlockClicked(
                        sender = UI.blocksMap.getValue(key=day)[row],
                        context = this
                    )
                }
            }
        }

        /* Hiding the floating button when user scrolls the agenda and showing it after some time */
        UI.agendaBodyScroll.setOnScrollChangeListener { _ :View, _ :Int, _ :Int, _ :Int, _ :Int ->
            if (UI.toggleFullScreenButton.visibility == View.VISIBLE) {
                UI.toggleFullScreenButton.visibility = View.GONE
                UI.toggleFullScreenButton.postDelayed({
                    UI.toggleFullScreenButton.visibility = View.VISIBLE
                }, this.ONSCROLL_BUTTON_RESPAWN_TIME+120) // <- this tiny extra delay causes a nice translation animation, besides the fade in, for this button
            }
            if (UI.saveAsImgButton.visibility == View.VISIBLE) {
                UI.saveAsImgButton.visibility = View.GONE
                UI.saveAsImgButton.postDelayed({
                    UI.saveAsImgButton.visibility = View.VISIBLE
                }, this.ONSCROLL_BUTTON_RESPAWN_TIME)
            }
        }

        AgendaWorker.buildAgenda(activity=this)
    }

    /**
     * Re-enabling fullscreen mode after dismissing a dialog, which
     * causes to exit fullscreen mode
     */
    override fun onWindowFocusChanged(hasFocus :Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && this.isFullScreenOn) { this.enterFullScreen() }
    }

    private var isFullScreenOn :Boolean = false // says if the system UI is hidden (i.e. full screen mode is activated).

    /**
     * Switches between full screen and normal screen modes
     */
    private fun toggleFullScreen() {
        if (this.isFullScreenOn) {
            this.exitFullScreen()
            UI.toggleFullScreenButton.setImageResource(R.drawable.fullscreen_icon)
            UI.toggleFullScreenButton.setColorFilter(Color.WHITE)
        } else {
            this.enterFullScreen()
            UI.toggleFullScreenButton.setImageResource(R.drawable.exit_fullscreen_icon)
            UI.toggleFullScreenButton.setColorFilter(Color.WHITE)
        }
        isFullScreenOn = !isFullScreenOn
    }

    /**
     * Encapsulates the methods for agenda building.
     */
    private object AgendaWorker {
        private val agendaData :MutableList<AgendaBlock> = mutableListOf()

        data class AgendaBlock(
            val button :Button,
            val events :MutableList<RamoEvent>
        )
        fun MutableList<AgendaBlock>.findEventsOf(button :Button) : MutableList<RamoEvent>? {
            this.forEach {
                if (it.button == button) { return it.events }
            }
            return null
        }

        /**
         * Click listener for an agenda block button.
         */
        public fun onBlockClicked(sender :Button, context : Context) {
            val blockEvents :MutableList<RamoEvent> = this.agendaData.findEventsOf(button = sender)!!
            if (blockEvents.count() == 0) { return }

            var info :String
            if (blockEvents.count() == 1) {
                info = blockEvents[0].toShortString()
            }
            else { // conflict: more than one event on the same block
                info = "Múltiples eventos (conflicto):\n"
                for (event :RamoEvent in blockEvents) {
                    info += "• %s\n".format(event.toShortString())
                }
                info.trim() // removing last line break
            }

            context.infoDialog(
                title = "Información de bloque",
                message = info
            )
        }

        /**
         * Displays the non-evaluation events for each user inscribed `Ramo`s in the agenda
         */
        public fun buildAgenda(activity :Activity) {
            Logf("[AgendaLandscapeActivity] Building agenda...")

            /* initializing */
            this.agendaData.clear()
            UI.blocksMap.forEach { (_ :DayOfWeek, buttons :List<Button>) ->
                buttons.forEach {
                    this.agendaData.add(AgendaBlock(button = it, events = mutableListOf()))
                }
            }

            AsyncTask.execute {
                /* filling `agendaData` i.e. mapping agenda block buttons with corresponding event(s) */
                var rowInterval :Pair<Int, Int> // ~ (rowStart, rowEnd)
                DataMaster.getEventsByWeekDay().forEach { (day :DayOfWeek, events :List<RamoEvent>) ->
                    for (ev : RamoEvent in events) {
                        rowInterval = timesToBlockIndexes(start = ev.startTime, end = ev.endTime)!!
                        for (rowNum :Int in (rowInterval.first until rowInterval.second)) {
                            this.agendaData.findEventsOf(
                                button = UI.blocksMap.getValue(key = day)[rowNum]
                            )!!.add(ev)
                        }
                    }
                }

                /* displaying events assigned to each block button */
                activity.runOnUiThread {
                    var break_loop :Boolean = false
                    this.agendaData.forEach { (block :Button, events :MutableList<RamoEvent>) ->
                        if (break_loop) { return@forEach }
                        events.forEachIndexed { index :Int, event : RamoEvent ->
                            if (index == 0) {
                                block.text = DataMaster.findRamo(
                                    NRC = event.ramoNRC,
                                    searchInUserList = true
                                )!!.nombre
                                val backColor :Int? = when(event.type) { // setting background color according to eventType
                                    RamoEventType.CLAS -> { activity.getColor(R.color.clas) }
                                    RamoEventType.AYUD -> { activity.getColor(R.color.ayud) }
                                    RamoEventType.LABT, RamoEventType.TUTR -> { activity.getColor(R.color.labt) }
                                    else -> { null } // <- will never happen, unless dumb code mistake
                                }
                                block.setBackgroundColor(backColor!!)
                            }
                            else {
                                block.setBackgroundColor( activity.getColor(R.color.conflict_background) ) // visibly marking block as conflicted
                                // concatenating multiple events in same block
                                block.text = "%s + %s".format(
                                    block.text, DataMaster.findRamo(
                                        NRC = event.ramoNRC,
                                        searchInUserList = true
                                    )!!.nombre
                                )
                                if (block.text.toString().filter{ it == '+' }.count() >= 2) { // if three or more events are in the same block, avoid showing all of them. They all can be viewed when on click.
                                    block.text = "%s + ...".format(block.text)
                                    break_loop = true // finishing this loop and parent loop
                                    return@forEach
                                }
                            }
                        }
                    }
                    Logf("[AgendaLandscapeActivity] Agenda successfully built.")
                }
            }
        }

        private val supportedHours_start :List<Int> = (8..21).toList() // [8, 21]
        private val supportedHours_end :List<Int> = (9..22).toList() // [9, 22]
        private fun timesToBlockIndexes(start :LocalTime, end :LocalTime) : Pair<Int, Int>? {
            var rowStart :Int = this.supportedHours_start.indexOf(start.hour)
            var rowEnd   :Int = this.supportedHours_end.indexOf(end.hour)+1
            if (rowStart == -1 || rowEnd == -1) { return null } // invalid hour(s): block wouldn't fit in agenda
            if (start.minute > 30) { rowStart+=1 }
            if (end.minute > 20) { rowEnd+=1 }
            return Pair(rowStart, rowEnd)
        }
    }
}