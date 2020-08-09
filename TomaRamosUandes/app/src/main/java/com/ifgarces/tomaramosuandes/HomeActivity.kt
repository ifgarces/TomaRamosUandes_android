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
        lateinit var topBar             :MaterialToolbar
        lateinit var catalogButton      :Button
        lateinit var userRamosContainer :View // ConstraintLayout
        lateinit var ramosRecycler      :RecyclerView
        lateinit var emptyRecyclerText  :TextView
        lateinit var creditosCounter    :TextView
        lateinit var agendaButton       :Button
        lateinit var pruebasButton      :Button

        lateinit var loadBackground  :View
        lateinit var loadProgressBar :View //ProgressBar

        fun init(owner :AppCompatActivity) {
            this.topBar             = owner.findViewById(R.id.home_topbar)
            this.catalogButton      = owner.findViewById(R.id.home_catalogButton)
            this.userRamosContainer = owner.findViewById(R.id.home_userRamosContainer)
            this.ramosRecycler      = owner.findViewById(R.id.home_ramosRecycler)
            this.emptyRecyclerText  = owner.findViewById(R.id.home_emptyRecyclerText)
            this.creditosCounter    = owner.findViewById(R.id.home_creditos)
            this.agendaButton       = owner.findViewById(R.id.home_agendaButton)
            this.pruebasButton      = owner.findViewById(R.id.home_pruebasButton)
            this.loadBackground     = owner.findViewById(R.id.home_loadBackground)
            this.loadProgressBar    = owner.findViewById(R.id.home_loadProgressBar)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_home)
        UI.init(owner=this)

        UI.ramosRecycler.layoutManager = LinearLayoutManager(this)
        UI.ramosRecycler.adapter = TakenRamosAdapter(data=DataMaster.getUserRamos())
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
        UI.catalogButton.setOnClickListener { this.launchCatalogView() }
        UI.agendaButton.setOnClickListener { this.launchAgendaView() }
        UI.pruebasButton.setOnClickListener { this.launchPruebasView() }
    }

    override fun onResume() {
        super.onResume()
        UI.loadBackground.visibility = View.GONE
        UI.loadProgressBar.visibility = View.GONE

        UI.emptyRecyclerText.visibility = View.INVISIBLE
        UI.userRamosContainer.visibility = View.VISIBLE

        UI.creditosCounter.text = DataMaster.getUserCreditsCount().toString()
        if (UI.creditosCounter.text == "0") {
            UI.emptyRecyclerText.visibility = View.VISIBLE
            UI.userRamosContainer.visibility = View.INVISIBLE
        }

        UI.ramosRecycler.adapter!!.notifyDataSetChanged() // <- as it is very hard to notify this adapter from another activity (`CatalogActivity`).
    }

    override fun onDestroy() {
        super.onDestroy()
        this.finishAffinity()
    }

    /* Displays a big `ProgressBar` spinner on top of everything */
    private fun enterLoadMode() {
        UI.loadBackground.visibility = View.VISIBLE
        UI.loadProgressBar.visibility = View.VISIBLE
    }

    private fun launchCatalogView() {
        this.enterLoadMode()
        this.startActivity(
            Intent(this, CatalogActivity::class.java)
        )
    }

    private fun launchAgendaView() {
        this.startActivity(
            Intent(this, AgendaActivity::class.java)
        )
    }

    private fun launchPruebasView() {
        // TODO: pruebas activity
    }

    private fun showHelpDialog() {
        // TODO: use Activity.infoDialog and show app version, author and usage (video?)
    }
}