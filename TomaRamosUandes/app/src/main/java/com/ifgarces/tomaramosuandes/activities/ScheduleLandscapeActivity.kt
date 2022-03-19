package com.ifgarces.tomaramosuandes.activities

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import com.ifgarces.tomaramosuandes.utils.*
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.concurrent.Executors


/**
 * For displaying the schedule in a landscape setting. This activity is forced to be landscape
 * regarding the device orientation sensor.
 * @property ONSCROLL_BUTTON_RESPAWN_TIME Time (milliseconds) passed between the
 * FloatingActionButton dissapears due scrolling and it appears again.
 * @property ROWS_COUNT Number of rows of schedule i.e. number of blocks per day of week.
 * @property isFullScreenOn Whether the system UI is hidden (i.e. full screen mode is activated).
 */
class ScheduleLandscapeActivity : AppCompatActivity() {

    companion object {
        const val ONSCROLL_BUTTON_RESPAWN_TIME :Long = 1500
        const val ROWS_COUNT :Int = 14
    }

    private class ActivityUI(owner :AppCompatActivity) {
        val saveAsImgButton :FloatingActionButton =
            owner.findViewById(R.id.landSchedule_saveAsImage)
        val toggleFullScreenButton :FloatingActionButton =
            owner.findViewById(R.id.landSchedule_toggleFullScreen)
        val scheduleBodyScroll :View =
            owner.findViewById(R.id.landSchedule_bodyScrollView) // ScrollView
        val scheduleBodyLayout :View =
            owner.findViewById(R.id.landSchedule_bodyLayout) // LinearLayout
        val blocksMap :Map<DayOfWeek, List<Button>> = mapOf(
            DayOfWeek.MONDAY to listOf(
                owner.findViewById(R.id.landSchedule_lun0),
                owner.findViewById(R.id.landSchedule_lun1),
                owner.findViewById(R.id.landSchedule_lun2),
                owner.findViewById(R.id.landSchedule_lun3),
                owner.findViewById(R.id.landSchedule_lun4),
                owner.findViewById(R.id.landSchedule_lun5),
                owner.findViewById(R.id.landSchedule_lun6),
                owner.findViewById(R.id.landSchedule_lun7),
                owner.findViewById(R.id.landSchedule_lun8),
                owner.findViewById(R.id.landSchedule_lun9),
                owner.findViewById(R.id.landSchedule_lun10),
                owner.findViewById(R.id.landSchedule_lun11),
                owner.findViewById(R.id.landSchedule_lun12),
                owner.findViewById(R.id.landSchedule_lun13)
            ),
            DayOfWeek.TUESDAY to listOf(
                owner.findViewById(R.id.landSchedule_mar0),
                owner.findViewById(R.id.landSchedule_mar1),
                owner.findViewById(R.id.landSchedule_mar2),
                owner.findViewById(R.id.landSchedule_mar3),
                owner.findViewById(R.id.landSchedule_mar4),
                owner.findViewById(R.id.landSchedule_mar5),
                owner.findViewById(R.id.landSchedule_mar6),
                owner.findViewById(R.id.landSchedule_mar7),
                owner.findViewById(R.id.landSchedule_mar8),
                owner.findViewById(R.id.landSchedule_mar9),
                owner.findViewById(R.id.landSchedule_mar10),
                owner.findViewById(R.id.landSchedule_mar11),
                owner.findViewById(R.id.landSchedule_mar12),
                owner.findViewById(R.id.landSchedule_mar13)
            ),
            DayOfWeek.WEDNESDAY to listOf(
                owner.findViewById(R.id.landSchedule_mie0),
                owner.findViewById(R.id.landSchedule_mie1),
                owner.findViewById(R.id.landSchedule_mie2),
                owner.findViewById(R.id.landSchedule_mie3),
                owner.findViewById(R.id.landSchedule_mie4),
                owner.findViewById(R.id.landSchedule_mie5),
                owner.findViewById(R.id.landSchedule_mie6),
                owner.findViewById(R.id.landSchedule_mie7),
                owner.findViewById(R.id.landSchedule_mie8),
                owner.findViewById(R.id.landSchedule_mie9),
                owner.findViewById(R.id.landSchedule_mie10),
                owner.findViewById(R.id.landSchedule_mie11),
                owner.findViewById(R.id.landSchedule_mie12),
                owner.findViewById(R.id.landSchedule_mie13)
            ),
            DayOfWeek.THURSDAY to listOf(
                owner.findViewById(R.id.landSchedule_jue0),
                owner.findViewById(R.id.landSchedule_jue1),
                owner.findViewById(R.id.landSchedule_jue2),
                owner.findViewById(R.id.landSchedule_jue3),
                owner.findViewById(R.id.landSchedule_jue4),
                owner.findViewById(R.id.landSchedule_jue5),
                owner.findViewById(R.id.landSchedule_jue6),
                owner.findViewById(R.id.landSchedule_jue7),
                owner.findViewById(R.id.landSchedule_jue8),
                owner.findViewById(R.id.landSchedule_jue9),
                owner.findViewById(R.id.landSchedule_jue10),
                owner.findViewById(R.id.landSchedule_jue11),
                owner.findViewById(R.id.landSchedule_jue12),
                owner.findViewById(R.id.landSchedule_jue13)
            ),
            DayOfWeek.FRIDAY to listOf(
                owner.findViewById(R.id.landSchedule_vie0),
                owner.findViewById(R.id.landSchedule_vie1),
                owner.findViewById(R.id.landSchedule_vie2),
                owner.findViewById(R.id.landSchedule_vie3),
                owner.findViewById(R.id.landSchedule_vie4),
                owner.findViewById(R.id.landSchedule_vie5),
                owner.findViewById(R.id.landSchedule_vie6),
                owner.findViewById(R.id.landSchedule_vie7),
                owner.findViewById(R.id.landSchedule_vie8),
                owner.findViewById(R.id.landSchedule_vie9),
                owner.findViewById(R.id.landSchedule_vie10),
                owner.findViewById(R.id.landSchedule_vie11),
                owner.findViewById(R.id.landSchedule_vie12),
                owner.findViewById(R.id.landSchedule_vie13)
            )
        )
    }

    private lateinit var UI :ActivityUI

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(
            if (DataMaster.user_stats.nightModeOn) R.layout.night_activity_schedule_landscape
            else R.layout.activity_schedule_landscape
        )
        this.UI = ActivityUI(owner = this)

        UI.saveAsImgButton.setColorFilter(Color.WHITE)
        UI.saveAsImgButton.setOnClickListener {
            Logf.debug(this::class, "Exporting schedule as image...")
            if (!ImageExportHandler.exportWeekScheduleImage(
                activity = this,
                targetView = UI.scheduleBodyScroll,
                tallView = UI.scheduleBodyLayout
            )) {
                ImageExportHandler.showImageExportErrorDialog(this)
            }
        }
        UI.toggleFullScreenButton.setColorFilter(Color.WHITE)
        UI.toggleFullScreenButton.setOnClickListener {
            this.toggleFullScreen()
        }

        // Setting click listeners for each corresponding schedule block
        for (row :Int in (0 until ROWS_COUNT)) {
            for (day :DayOfWeek in listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )) {
                UI.blocksMap.getValue(key = day)[row].text = ""
                UI.blocksMap.getValue(key = day)[row].setOnClickListener {
                    ScheduleWorker.onBlockClicked(
                        sender = UI.blocksMap.getValue(key = day)[row],
                        context = this
                    )
                }
            }
        }

        // Hiding the floating button when user scrolls the schedule and showing it after some time
        UI.scheduleBodyScroll.setOnScrollChangeListener { _ :View, _ :Int, _ :Int, _ :Int, _ :Int ->
            if (UI.toggleFullScreenButton.visibility == View.VISIBLE) {
                UI.toggleFullScreenButton.visibility = View.GONE
                UI.toggleFullScreenButton.postDelayed({
                    UI.toggleFullScreenButton.visibility = View.VISIBLE
                }, ONSCROLL_BUTTON_RESPAWN_TIME + 120) // this tiny extra delay causes a nice translation animation, besides the fade in, for this button
            }
            if (UI.saveAsImgButton.visibility == View.VISIBLE) {
                UI.saveAsImgButton.visibility = View.GONE
                UI.saveAsImgButton.postDelayed({
                    UI.saveAsImgButton.visibility = View.VISIBLE
                }, ONSCROLL_BUTTON_RESPAWN_TIME)
            }
        }

        ScheduleWorker.buildSchedule(activity = this, blocksMap = UI.blocksMap)

        // Disabling floating actions when there is not a single `Ramo` to display
        if (DataMaster.user_ramos.count() == 0) {
            listOf(UI.saveAsImgButton, UI.toggleFullScreenButton).forEach { floatingButt :FloatingActionButton ->
                floatingButt.isEnabled = false
            }
        }
    }

    /**
     * Re-enabling fullscreen mode after dismissing a dialog, which causes to exit fullscreen mode.
     */
    override fun onWindowFocusChanged(hasFocus :Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && this.isFullScreenOn) this.enterFullScreen()
    }

    private var isFullScreenOn :Boolean = false

    /**
     * Switches between full screen and normal screen modes.
     */
    private fun toggleFullScreen() {
        if (this.isFullScreenOn) {
            this.exitFullScreen()
            UI.toggleFullScreenButton.setImageResource(R.drawable.fullscreen_icon)
            UI.toggleFullScreenButton.setColorFilter(Color.WHITE) // color filter must be aplied again, as the drawable gets refreshed
        } else {
            this.enterFullScreen()
            UI.toggleFullScreenButton.setImageResource(R.drawable.exit_fullscreen_icon)
            UI.toggleFullScreenButton.setColorFilter(Color.WHITE)
        }
        isFullScreenOn = !isFullScreenOn
    }

    /**
     * Encapsulates the methods for weekly schedule building.
     */
    private object ScheduleWorker {
        private val scheduleData :MutableList<ScheduleBlock> = mutableListOf()

        data class ScheduleBlock(
            val button :Button,
            val events :MutableList<RamoEvent>
        )

        fun MutableList<ScheduleBlock>.findEventsOf(button :Button) :MutableList<RamoEvent> {
            return this.first { block :ScheduleBlock ->
                block.button == button
            }.events
        }

        /**
         * Click listener for an schedule block button.
         */
        public fun onBlockClicked(sender :Button, context :Context) {
            val blockEvents :MutableList<RamoEvent> = this.scheduleData.findEventsOf(
                button = sender
            )
            if (blockEvents.count() == 0) return

            var info :String
            if (blockEvents.count() == 1) {
                info = blockEvents[0].toShortString()
            } else { // conflict: more than one event on the same block
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
         * Displays the non-evaluation events for each user inscribed `Ramo`s in the schedule.
         */
        public fun buildSchedule(activity :Activity, blocksMap :Map<DayOfWeek, List<Button>>) {
            Logf.debug(this::class, "Building week schedule...")
            val startMillis :Long = System.currentTimeMillis()
            val isNightModeOn :Boolean = (DataMaster.user_stats.nightModeOn)

            // Initializing
            this.scheduleData.clear()
            blocksMap.forEach { (_ :DayOfWeek, buttons :List<Button>) ->
                buttons.forEach {
                    this.scheduleData.add(ScheduleBlock(button = it, events = mutableListOf()))
                }
            }

            Executors.newSingleThreadExecutor().execute {
                // Filling `scheduleData` i.e. mapping schedule block buttons with corresponding
                // event(s)
                var rowInterval :Pair<Int, Int> // ~ (rowStart, rowEnd)
                DataMaster.getEventsByWeekDay()
                    .forEach { (day :DayOfWeek, events :List<RamoEvent>) ->
                        for (ev :RamoEvent in events) {
                            rowInterval = this.timesToBlockIndexes(
                                start = ev.startTime, end = ev.endTime
                            )!!
                            for (rowNum :Int in (rowInterval.first until rowInterval.second)) {
                                this.scheduleData.findEventsOf(
                                    button = blocksMap.getValue(key = day)[rowNum]
                                ).add(ev)
                            }
                        }
                    }

                // Displaying events assigned to each block button (modifying Views on UI thread)
                activity.runOnUiThread {
                    this.scheduleData.forEach { (block :Button, events :MutableList<RamoEvent>) ->
                        // Setting the display text for this block, given its events
                        block.text = this.eventsOfBlockToString(events)

                        // Setting block background color
                        if (events.count() == 1) {
                            val event :RamoEvent = events.first()
                            block.setBackgroundColor(
                                when (event.type) {
                                    RamoEventType.CLAS -> activity.getColor(
                                        if (isNightModeOn) R.color.night_blockColor_clas
                                        else R.color.blockColor_clas
                                    )
                                    RamoEventType.AYUD -> activity.getColor(
                                        if (isNightModeOn) R.color.night_blockColor_ayud
                                        else R.color.blockColor_ayud
                                    )
                                    RamoEventType.LABT, RamoEventType.TUTR -> activity.getColor(
                                        if (isNightModeOn) R.color.night_blockColor_labt
                                        else R.color.blockColor_labt
                                    )
                                    else -> throw Exception(
                                        "Unknown type %d for %s".format(event.type, event) // will never happen, unless dumb code mistake
                                    )
                                }
                            )
                        } else if (events.count() > 1) {
                            block.setBackgroundColor(activity.getColor(
                                if (isNightModeOn) R.color.night_conflict_background
                                else R.color.conflict_background
                            ))
                        }
                    }
                    Logf.debug(
                        this::class,
                        "Landscape eeek shcedule built on %.2f seconds".format(
                            (System.currentTimeMillis() - startMillis).toDouble() / 1000.0
                        )
                    )
                }
            }
        }

        // Defining supported time interval for displaying in the schedule table blocks
        private val supportedHours_start :List<Int> = (8..21).toList() // [8, 21]
        private val supportedHours_end :List<Int> = (9..22).toList() // [9, 22]

        /**
         * Given a `start` and `end` time objects, returns the indexes of the block rows
         * corresponding for that times, as a pair `(startRowIndex, endRowIndex)`.
         */
        private fun timesToBlockIndexes(start :LocalTime, end :LocalTime) :Pair<Int, Int>? {
            var rowStart :Int = this.supportedHours_start.indexOf(start.hour)
            var rowEnd :Int = this.supportedHours_end.indexOf(end.hour) + 1
            if (rowStart == -1 || rowEnd == -1) return null // invalid hour(s): block wouldn't fit in schedule
            if (start.minute > 30) rowStart++
            if (end.minute > 20) rowEnd++
            return Pair(rowStart, rowEnd)
        }

        /**
         * Returns a proper string representing a collection of `RamoEvent`s, useful for when there
         * are multiple events in, for instance, a single week schedule block.
         * @param eventCollection The `RamoEvent`s belonging to a schedule block we desire to get
         * the display text for.
         * @returns A short string with the `RamoEvent`'s `Ramo` name and event location if the list
         * has only one item, otherwise, a concatenation of event's `Ramo` names (with a maximum,
         * avoiding the string to become indefinetly large.
         */
        private fun eventsOfBlockToString(eventCollection :List<RamoEvent>) :String {
            val count :Int = eventCollection.count()
            if (count == 0) return ""
            // Single event block
            if (count == 1) {
                val event :RamoEvent = eventCollection.first()
                return "%s%s".format(
                    DataMaster.findRamo(
                        NRC = event.ramoNRC,
                        searchInUserList = true
                    )!!.nombre,
                    if (event.location != "") "\n[%s]".format(event.location) else ""
                )
            }
            // Will display only information on the first two events (not including location)
            val firstRamos :List<Ramo> = eventCollection.subList(0, 2).map { event :RamoEvent ->
                DataMaster.findRamo(
                    NRC = event.ramoNRC,
                    searchInUserList = true
                )!!
            }
            var result :String = "%s + %s".format(firstRamos[0].nombre, firstRamos[1].nombre)
            if (count > 2) result += "\n(+%d)".format(count - 2)
            return result
        }
    }
}