package com.ifgarces.tomaramosuandes

import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifgarces.tomaramosuandes.models.Curso


class AgendaActivity : AppCompatActivity() {

    private object PortraitUI { // portrait orientation widgets
        lateinit var fullScreenAction :FloatingActionButton
        lateinit var dayRecyclers     :List<RecyclerView>
        lateinit var dayHeaders       :List<TextView>
        val MON :Int = 0 // lists indexes (day markers)
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

    private object LandscapeUI { // landscape orientation widgets
        lateinit var saveAsImgAction :FloatingActionButton
        lateinit var lun             :List<Button>
        lateinit var mar             :List<Button>
        lateinit var mie             :List<Button>
        lateinit var jue             :List<Button>
        lateinit var vie             :List<Button>

        fun init(owner :AppCompatActivity) {
            this.saveAsImgAction = owner.findViewById(R.id.bigAgenda_saveAsImage)
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

    private val blockTimes :List<String> = listOf(
        "8:30",
        "9:30",
        "10:30",
        "11:30",
        "12:30",
        "13:30",
        "14:30",
        "15:30",
        "16:30",
        "17:30",
        "18:30",
        "19:30",
        "20:30",
        "21:30"
    )

//    override fun onResume() {
//        super.onResume()
//
//        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            this.portraitModeInnit()
//            // TODO: build portrait agenda (use different frafments instead?)
//        }
//        else {
//            this.landscapeModeInit()
//            // TODO: build landscape agenda
//        }
//    }

    private fun portraitModeInnit() {
        this.setContentView(R.layout.agenda_portrait)
        PortraitUI.init(owner = this)

        PortraitUI.fullScreenAction.setColorFilter(Color.WHITE)
        PortraitUI.fullScreenAction.setOnClickListener { this.landscapeModeInit() }
    }

    private fun landscapeModeInit() {
        this.setContentView(R.layout.agenda_landscape)
        this.enterFullScreen()
        LandscapeUI.init(owner=this)

        LandscapeUI.saveAsImgAction.setColorFilter(Color.WHITE)
        LandscapeUI.saveAsImgAction.setOnClickListener { this.exportAgendaImage() }
        for (k :Int in (0..13)) {
            for (collection :List<Button> in listOf(LandscapeUI.lun, LandscapeUI.mar, LandscapeUI.mie, LandscapeUI.jue, LandscapeUI.vie)) {
                collection[k].text = ""
                collection[k].setOnClickListener { this.blockClick(sender=collection[k]) }
            }
        }
    }

    private fun exportAgendaImage() {

    }

    private fun updateAgenda(cursos :List<Curso>) {
        // TODO: make agenda construction algorithm
    }

    private fun blockClick(sender :Button) {
        if (sender.text == "") return
        // TODO: handle agenda element click. Show block details (may contain multiple Curso events, on conflict)
    }

    private fun rowToTime(index :Int) : String {
        return this.blockTimes[index]
    }

    /* Reverse of `blockIndexToTime` */
    private fun timeToRow(time :String) : Int {
        // TODO: complete this
        return -1
    }

    /* Enters "inmersive mode", hiding system LandscapeUI elements (and forcing landscape orientation) */
    private fun enterFullScreen() { /// references: https://developer.android.com/training/system-ui/immersive.html
        this.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}