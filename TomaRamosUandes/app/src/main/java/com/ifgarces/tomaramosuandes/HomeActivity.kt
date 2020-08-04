package com.ifgarces.tomaramosuandes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


/**
 * Holds the main menu, etc.
 */
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_home)

        // TODO: setup main menu

        this.launchAgendaView()
    }

    private fun launchAgendaView() {
        this.startActivity(
            Intent(this, AgendaActivity::class.java)
        )
    }
}