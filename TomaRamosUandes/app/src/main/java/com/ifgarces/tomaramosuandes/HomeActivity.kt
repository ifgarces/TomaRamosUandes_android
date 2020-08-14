package com.ifgarces.tomaramosuandes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.ifgarces.tomaramosuandes.adapters.RamosAdapter
import com.ifgarces.tomaramosuandes.utils.Logf


/**
 * Holds the main menu, etc.
 */
class HomeActivity : AppCompatActivity() {

    public object RecyclerSync {
        private var updatePending :Boolean = false
        private fun notifyAdapter() = UI.ramosRecycler.adapter!!.notifyDataSetChanged()
        public fun updateRecycler() {
            try {
                this.notifyAdapter()
                Logf("[HomeActivity] RecyclerSync: recycler updated")
                this.updatePending = false
            }
            catch (e :NullPointerException) {
                Logf("[HomeActivity] RecyclerSync: recycler update queued")
                this.updatePending = true // update is queued and will be executed when this activity starts again
            }
        }
    }

    private object UI {
        lateinit var topBar             :MaterialToolbar
        lateinit var catalogButton      :Button
        lateinit var ramosRecycler      :RecyclerView
        lateinit var emptyRecyclerText  :TextView
        lateinit var creditosCounter    :TextView
        lateinit var agendaButton       :Button
        lateinit var pruebasButton      :Button
        lateinit var loadDisplay        :View

        fun init(owner :AppCompatActivity) {
            this.topBar             = owner.findViewById(R.id.home_topbar)
            this.catalogButton      = owner.findViewById(R.id.home_catalogButton)
            this.ramosRecycler      = owner.findViewById(R.id.home_ramosRecycler)
            this.emptyRecyclerText  = owner.findViewById(R.id.home_emptyRecyclerText)
            this.creditosCounter    = owner.findViewById(R.id.home_creditos)
            this.agendaButton       = owner.findViewById(R.id.home_agendaButton)
            this.pruebasButton      = owner.findViewById(R.id.home_pruebasButton)
            this.loadDisplay        = owner.findViewById(R.id.home_loadScreen)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_home)
        UI.init(owner=this)

        UI.ramosRecycler.layoutManager = LinearLayoutManager(this)
        UI.ramosRecycler.adapter = RamosAdapter(data=DataMaster.getUserRamos())
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
        UI.loadDisplay.visibility = View.GONE

        UI.creditosCounter.text = DataMaster.getUserTotalCredits().toString()
        if (UI.creditosCounter.text == "0") {
            UI.ramosRecycler.visibility = View.INVISIBLE
        }

        RecyclerSync.updateRecycler()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.finishAffinity()
    }

    private fun launchCatalogView() {
        UI.loadDisplay.visibility = View.VISIBLE
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