package com.ifgarces.tomaramosuandes

import android.content.pm.ActivityInfo
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

        // TODO: fix AgendaLandscapeFragment being initialized several times due the following orientation rule

        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.launchPortraitMode()
        } else {
            this.launchLandscapeMode()
        }
    }

    /**
     * In `AgendaLandscapeFragment`, re-enabling fullscreen mode after dismissing a dialog, which
     * causes to exit fullscreen mode
     */
    override fun onWindowFocusChanged(hasFocus :Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && this.inLandscapeMode) { this.enterFullScreen() }
    }

    private fun launchPortraitMode() {
        AgendaPortraitFragment.summon(caller=this, targetView=R.id.agenda_fragmentContainer)
        this.inLandscapeMode = false
    }

    private fun launchLandscapeMode() {
        AgendaLandscapeFragment.summon(caller=this, targetView=R.id.agenda_fragmentContainer)
        this.inLandscapeMode = true
    }
}