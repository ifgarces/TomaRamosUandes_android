package com.ifgarces.tomaramosuandes

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class AgendaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_agenda)

        AgendaPortraitFragment.showNow(caller=this, targetView=R.id.agenda_fragmentContainer)
    }

    override fun onResume() {
        super.onResume()

        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            AgendaPortraitFragment.showNow(caller=this, targetView=R.id.agenda_fragmentContainer)
        }
        else {
            AgendaLandscapeFragment.showNow(caller=this, targetView=R.id.agenda_fragmentContainer)
        }
    }
}