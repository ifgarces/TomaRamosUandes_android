package com.ifgarces.tomaramosuandes

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ifgarces.tomaramosuandes.utils.enterFullScreen


class AgendaActivity : AppCompatActivity() {

    var inLandscapeMode :Boolean = false // indicates if `AgendaLandscapeFragment` is executing now

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_agenda)
    }

    override fun onResume() {
        super.onResume()

        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            AgendaPortraitFragment.summon(caller=this, targetView=R.id.agenda_fragmentContainer)
            inLandscapeMode = false
        }
        else {
            AgendaLandscapeFragment.summon(caller=this, targetView=R.id.agenda_fragmentContainer)
            inLandscapeMode = true
        }
    }

    /**
     * In `AgendaLandscapeFragment`, re-enabling fullscreen mode after dismissing a dialog that
     * caused to exit fullscreen mode
     */
    override fun onWindowFocusChanged(hasFocus :Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && inLandscapeMode) { this.enterFullScreen() }
    }
}