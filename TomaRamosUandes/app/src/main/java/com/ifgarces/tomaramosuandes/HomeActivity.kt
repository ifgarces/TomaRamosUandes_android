package com.ifgarces.tomaramosuandes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.ifgarces.tomaramosuandes.adapters.TakenRamosAdapter
import com.ifgarces.tomaramosuandes.utils.Logf


/**
 * Holds the main menu, etc.
 */
class HomeActivity : AppCompatActivity() {

    private object UI {
        lateinit var topBar            :MaterialToolbar
        lateinit var catalogButton     :Button
        lateinit var ramosRecycler    :RecyclerView
        lateinit var emptyRecyclerText :TextView
        lateinit var creditosStaticText :TextView
        lateinit var creditosCounter    :TextView
        lateinit var agendaButton      :Button
        lateinit var pruebasButton     :Button

        lateinit var loadBackground  :View
        lateinit var loadProgressBar :ProgressBar

        fun init(owner :AppCompatActivity) {
            this.topBar            = owner.findViewById(R.id.home_topbar)
            this.catalogButton     = owner.findViewById(R.id.home_catalogButton)
            this.ramosRecycler    = owner.findViewById(R.id.home_ramosRecycler)
            this.emptyRecyclerText = owner.findViewById(R.id.home_emptyRecyclerText)
            this.creditosStaticText = owner.findViewById(R.id.home_creditosStatic)
            this.creditosCounter    = owner.findViewById(R.id.home_creditos)
            this.agendaButton      = owner.findViewById(R.id.home_agendaButton)
            this.pruebasButton     = owner.findViewById(R.id.home_pruebasButton)
            this.loadBackground    = owner.findViewById(R.id.home_loadBackground)
            this.loadProgressBar   = owner.findViewById(R.id.home_loadProgressBar)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_home)
        UI.init(owner=this)

        UI.ramosRecycler.layoutManager = LinearLayoutManager(this)
        UI.ramosRecycler.adapter =
            TakenRamosAdapter(data = DataMaster.getUserRamos())
        // [!] TODO: somehow set adapter data referenced to `DataMaster.userRamos`

        UI.topBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_help -> {
                    this.showHelpDialog()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    Logf("[HomeActivity] Warning: unknown toolbar element pressed (id=%d).", it.itemId)
                    return@setOnMenuItemClickListener true
                }
            }
        }
        UI.catalogButton.setOnClickListener {
            UI.loadBackground.visibility = View.VISIBLE
            UI.loadProgressBar.visibility = View.VISIBLE
            this.launchCatalogView()
        }
        UI.agendaButton.setOnClickListener {
            this.launchAgendaView()
        }
        UI.pruebasButton.setOnClickListener {
            this.launchPruebasView()
        }
    }

    override fun onResume() {
        super.onResume()
        UI.loadBackground.visibility = View.GONE
        UI.loadProgressBar.visibility = View.GONE

        UI.creditosCounter.text = DataMaster.getUserCreditsCount().toString()
        UI.emptyRecyclerText.visibility = View.GONE
        if (UI.creditosCounter.text == "0") {
            this.toggleVisibility(UI.emptyRecyclerText)
            this.toggleVisibility(UI.ramosRecycler)
            this.toggleVisibility(UI.creditosStaticText)
            this.toggleVisibility(UI.creditosCounter)
        }

        UI.ramosRecycler.adapter!!.notifyDataSetChanged() // <- as it is very hard to notify this adapter from another activity (`CatalogActivity`).
    }

    override fun onDestroy() {
        super.onDestroy()
        this.finishAffinity() // terminating the program instead of returning to `MainActivity`
    }

    /* Switches between VISIBLE and INVISIBLE  */
    private fun toggleVisibility(widget :View) {
        if (widget.visibility == View.VISIBLE) {
            widget.visibility = View.INVISIBLE
        }
        else {
            widget.visibility = View.VISIBLE
        }
    }

    private fun showHelpDialog() {
        // TODO: use Activity.infoDialog and show app version, author and usage (video?)
    }

    private fun launchAgendaView() {
        this.startActivity(
            Intent(this, AgendaActivity::class.java)
        )
    }

    private fun launchPruebasView() {
        // TODO: pruebas activity
    }

    private fun launchCatalogView() {
        this.startActivity(
            Intent(this, CatalogActivity::class.java)
        )
    }
}