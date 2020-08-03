package com.ifgarces.tomaramosuandes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_home)

        this.launchAgendaView()
    }

    private fun launchAgendaView() {
        this.startActivity(
            Intent(this, AgendaActivity::class.java)
        )
    }
}