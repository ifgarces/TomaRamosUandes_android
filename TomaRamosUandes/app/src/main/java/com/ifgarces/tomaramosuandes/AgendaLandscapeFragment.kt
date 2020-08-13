package com.ifgarces.tomaramosuandes

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
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import java.time.DayOfWeek
import java.time.LocalTime


class AgendaLandscapeFragment : Fragment() {


    private val supportedHours :List<Int> = (8 until 22).toList() // [8, 22]
    private data class AgendaBlock(
        val day        :DayOfWeek,
        val hour_index :Int, // corresponging index of `supportedHours`
        val event      :RamoEvent
    )
    private fun time_toBlockIndexes(time : LocalTime) : Int? {
        var blockIndex :Int = supportedHours.indexOf(element=time.hour)
        if (blockIndex == -1) { return null }
        if (time.minute >= 30) { blockIndex++ }
        return blockIndex
    }
    private fun drawEvent(ramo : Ramo, event : RamoEvent) {
        val startBlock :Int = time_toBlockIndexes(event.startTime)!!
        val endBlock :Int = time_toBlockIndexes(event.endTime)!!
        // TODO: may handle NullPointerException



    }

    public companion object {
        public fun summon(caller :FragmentActivity, widget_id :Int) {
            val transactioner :FragmentTransaction = caller.supportFragmentManager.beginTransaction()
                .replace(widget_id, this.newInstance())
            transactioner.commit()
        }
        private fun newInstance() = AgendaLandscapeFragment()
    }

    private object UI {
        lateinit var rootView        :View
        lateinit var saveAsImgAction :FloatingActionButton
        lateinit var lun             :List<Button>
        lateinit var mar             :List<Button>
        lateinit var mie             :List<Button>
        lateinit var jue             :List<Button>
        lateinit var vie             :List<Button>

        fun init(owner :View) {
            this.rootView = owner
            this.saveAsImgAction = owner.findViewById(R.id.landAgenda_saveAsImage)
            this.lun = listOf(
                owner.findViewById(R.id.lun0),
                owner.findViewById(R.id.lun1),
                owner.findViewById(R.id.lun2),
                owner.findViewById(R.id.lun3),
                owner.findViewById(R.id.lun4),
                owner.findViewById(R.id.lun5),
                owner.findViewById(R.id.lun6),
                owner.findViewById(R.id.lun7),
                owner.findViewById(R.id.lun8),
                owner.findViewById(R.id.lun9),
                owner.findViewById(R.id.lun10),
                owner.findViewById(R.id.lun11),
                owner.findViewById(R.id.lun12),
                owner.findViewById(R.id.lun13)
            )
            this.mar = listOf(
                owner.findViewById(R.id.mar0),
                owner.findViewById(R.id.mar1),
                owner.findViewById(R.id.mar2),
                owner.findViewById(R.id.mar3),
                owner.findViewById(R.id.mar4),
                owner.findViewById(R.id.mar5),
                owner.findViewById(R.id.mar6),
                owner.findViewById(R.id.mar7),
                owner.findViewById(R.id.mar8),
                owner.findViewById(R.id.mar9),
                owner.findViewById(R.id.mar10),
                owner.findViewById(R.id.mar11),
                owner.findViewById(R.id.mar12),
                owner.findViewById(R.id.mar13)
            )
            this.mie = listOf(
                owner.findViewById(R.id.mie0),
                owner.findViewById(R.id.mie1),
                owner.findViewById(R.id.mie2),
                owner.findViewById(R.id.mie3),
                owner.findViewById(R.id.mie4),
                owner.findViewById(R.id.mie5),
                owner.findViewById(R.id.mie6),
                owner.findViewById(R.id.mie7),
                owner.findViewById(R.id.mie8),
                owner.findViewById(R.id.mie9),
                owner.findViewById(R.id.mie10),
                owner.findViewById(R.id.mie11),
                owner.findViewById(R.id.mie12),
                owner.findViewById(R.id.mie13)
            )
            this.jue = listOf(
                owner.findViewById(R.id.jue0),
                owner.findViewById(R.id.jue1),
                owner.findViewById(R.id.jue2),
                owner.findViewById(R.id.jue3),
                owner.findViewById(R.id.jue4),
                owner.findViewById(R.id.jue5),
                owner.findViewById(R.id.jue6),
                owner.findViewById(R.id.jue7),
                owner.findViewById(R.id.jue8),
                owner.findViewById(R.id.jue9),
                owner.findViewById(R.id.jue10),
                owner.findViewById(R.id.jue11),
                owner.findViewById(R.id.jue12),
                owner.findViewById(R.id.jue13)
            )
            this.vie = listOf(
                owner.findViewById(R.id.vie0),
                owner.findViewById(R.id.vie1),
                owner.findViewById(R.id.vie2),
                owner.findViewById(R.id.vie3),
                owner.findViewById(R.id.vie4),
                owner.findViewById(R.id.vie5),
                owner.findViewById(R.id.vie6),
                owner.findViewById(R.id.vie7),
                owner.findViewById(R.id.vie8),
                owner.findViewById(R.id.vie9),
                owner.findViewById(R.id.vie10),
                owner.findViewById(R.id.vie11),
                owner.findViewById(R.id.vie12),
                owner.findViewById(R.id.vie13)
            )
        }
    }

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        UI.init(owner=inflater.inflate(R.layout.fragment_agenda_landscape, container, false))

        UI.saveAsImgAction.setColorFilter(Color.WHITE)
        UI.saveAsImgAction.setOnClickListener { this.exportImage() }
        for (k :Int in (0..13)) {
            for (collection :List<Button> in listOf(UI.lun, UI.mar, UI.mie, UI.jue, UI.vie)) {
                collection[k].text = ""
                collection[k].setOnClickListener { this.blockClick(sender=collection[k]) }
            }
        }

        this.enterFullScreen()
        this.buildAgenda()
        return UI.rootView
    }

    /* Enters "inmersive mode", hiding system LandscapeUI elements (and forcing landscape orientation) */
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

    private fun blockClick(sender :Button) {
        // TODO: fill
    }

    private fun buildAgenda() {
        // TODO: fill
    }

    private fun exportImage() {
        // TODO: fill
    }

    private val blockTimes :List<String> = listOf("8:30", "9:30", "10:30", "11:30", "12:30", "13:30", "14:30", "15:30", "16:30", "17:30", "18:30", "19:30", "20:30", "21:30")
    private fun rowToTime(index :Int) : String {
        return this.blockTimes[index]
    }

    private fun timeToRow(time_something :String) : Int {
        // TODO: fill
        return -1
    }
}