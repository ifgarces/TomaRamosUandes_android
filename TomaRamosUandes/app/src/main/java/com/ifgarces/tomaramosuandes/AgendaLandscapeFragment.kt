package com.ifgarces.tomaramosuandes

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
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
import java.time.LocalTime
import com.ifgarces.tomaramosuandes.utils.ImageWorker
import com.ifgarces.tomaramosuandes.utils.Logf


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
        lateinit var lun              :List<Button>
        lateinit var mar              :List<Button>
        lateinit var mie              :List<Button>
        lateinit var jue              :List<Button>
        lateinit var vie              :List<Button>

        fun init(owner :View) {
            this.rootView = owner
            this.saveAsImgAction = owner.findViewById(R.id.landAgenda_saveAsImage)
            this.agendaBodyScroll = owner.findViewById(R.id.landAgenda_bodyScrollView)
            this.agendaBodyLayout = owner.findViewById(R.id.landAgenda_bodyLayout)
            this.lun = listOf(
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
            )
            this.mar = listOf(
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
            )
            this.mie = listOf(
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
            )
            this.jue = listOf(
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
            )
            this.vie = listOf(
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
        }
    }

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        UI.init(owner=inflater.inflate(R.layout.fragment_agenda_landscape, container, false))

        UI.saveAsImgAction.setColorFilter(Color.WHITE)
        UI.saveAsImgAction.setOnClickListener {
            Logf("[AgendaLandscapeFragment] Exporting agenda as image...")
            ImageWorker.exportAgendaImage(
                context = this.context!!,
                targetView = UI.agendaBodyScroll,
                largerView = UI.agendaBodyLayout
            )
        }
        for (k :Int in (0..13)) {
            for (collection :List<Button> in listOf(UI.lun, UI.mar, UI.mie, UI.jue, UI.vie)) {
                collection[k].text = ""
//                collection[k].setOnClickListener {
//                    this.blockClick(sender=collection[k])
//                }
            }
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


    private object AgendaWorker { // encapsulating agenda builder-related functions, as it may be complex otherwise
        private data class AgendaBlock( // linking each agenda visible button to the event on it. If conflict, there will be multiple events on a block.
            val button :Button,
            val events :MutableList<RamoEvent>
        )
        private lateinit var data :MutableList<AgendaBlock>

        /* Requires `UI` to be already initialized. Builds the landscape agenda elements */
        fun init() { // TODO: fill `data`

            this.buildAgenda()
        }

        private fun buildAgenda() {

        }

        private fun drawEvent(ramo : Ramo, event : RamoEvent) {
            val startBlock :Int = timeToBlockIndex(event.startTime)!!
            val endBlock :Int = timeToBlockIndex(event.endTime)!!
            // TODO: may handle NullPointerException

        }

        private val supportedHours :List<Int> = (8 until 22).toList() // [8, 22]
        private fun timeToBlockIndex(time :LocalTime) : Int? {
            // there are 14 blocks i.e. block index in [0, 13]. They cover hours from 8:30-9:20 to 21:30:22:20
            var blockIndex :Int = supportedHours.indexOf(element=time.hour)
            if (blockIndex == -1) { return null } // time can't be shown in the agenda
            if (time.minute >= 30) { blockIndex++ }
            return blockIndex
        }
    }
}