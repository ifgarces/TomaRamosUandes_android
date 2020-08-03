package com.ifgarces.tomaramosuandes

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button


class AgendaActivity : AppCompatActivity() {

    private object UI {
        lateinit var lun :List<Button>
        lateinit var mar :List<Button>
        lateinit var mie :List<Button>
        lateinit var jue :List<Button>
        lateinit var vie :List<Button>

        fun init(owner :AppCompatActivity) {
            lun = listOf(
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
            mar = listOf(
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
            mie = listOf(
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
            jue = listOf(
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
            vie = listOf(
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

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.landscape_agenda) // <- !!
        UI.init(owner=this)

        for (k :Int in (0..13)) {
            for (collection :List<Button> in listOf(UI.lun, UI.mar, UI.mie, UI.jue, UI.vie)) {
                collection[k].text = ""
                collection[k].setOnClickListener { this.blockClick(sender=collection[k]) }
            }
        }
        this.enterFullScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.exitFullScreen()
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

    /* Enters "inmersive mode", hiding system UI elements (and forcing landscape orientation) */
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

    /* Undoes `enterFullScreen()` */
    private fun exitFullScreen() {
        this.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
}