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
import com.ifgarces.tomaramosuandes.adapters.CatalogRamosAdapter
import com.ifgarces.tomaramosuandes.utils.Logf


class HomeActivity : AppCompatActivity() {

    public object RecyclerSync { // allows to update the recycler from another activity/fragment
        private var updatePending :Boolean = false
        private fun notifyAdapter() = UI.ramosRecycler.adapter!!.notifyDataSetChanged()
        public fun requestUpdate() {
            try {
                this.notifyAdapter()
            }
            catch (e :NullPointerException) { //
                Logf("[HomeActivity] RecyclerSync: recycler update queued")
                this.updatePending = true // update is queued and will be executed when this activity starts again
                return
            }
            this.processRecyclerChange()
            Logf("[HomeActivity] RecyclerSync: recycler updated")
            this.updatePending = false
        }
        public fun updateLocal() {
            if (this.updatePending) { this.notifyAdapter() }
            this.processRecyclerChange()
        }
        private fun processRecyclerChange() {
            UI.creditosCounter.text = DataMaster.getUserTotalCredits().toString()
            if (UI.creditosCounter.text == "0") {
                UI.ramosRecycler.visibility = View.INVISIBLE
                UI.agendaButton.isEnabled = false
                UI.evaluationsButton.isEnabled = false
            } else {
                UI.ramosRecycler.visibility = View.VISIBLE
                UI.agendaButton.isEnabled = true
                UI.evaluationsButton.isEnabled = true
            }
        }
    }

    private object UI {
        lateinit var topBar              :MaterialToolbar
        lateinit var catalogButton       :Button
        lateinit var ramosRecycler       :RecyclerView
        lateinit var emptyRecyclerMarker :View // TextView
        lateinit var creditosCounter     :TextView
        lateinit var agendaButton        :Button
        lateinit var evaluationsButton   :Button
        lateinit var loadDisplay         :View

        fun init(owner :AppCompatActivity) {
            this.topBar             = owner.findViewById(R.id.home_topbar)
            this.catalogButton      = owner.findViewById(R.id.home_catalogButton)
            this.ramosRecycler      = owner.findViewById(R.id.home_ramosRecycler)
            this.emptyRecyclerMarker  = owner.findViewById(R.id.home_emptyRecyclerText)
            this.creditosCounter    = owner.findViewById(R.id.home_creditos)
            this.agendaButton       = owner.findViewById(R.id.home_agendaButton)
            this.evaluationsButton  = owner.findViewById(R.id.home_pruebasButton)
            this.loadDisplay        = owner.findViewById(R.id.home_loadScreen)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_home)
        UI.init(owner=this)

        UI.ramosRecycler.layoutManager = LinearLayoutManager(this)
        UI.ramosRecycler.adapter = CatalogRamosAdapter(
            data = DataMaster.getUserRamos(),
            allTaken = true
        )

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
        UI.evaluationsButton.setOnClickListener { this.launchEvaluationsView() }
    }

    override fun onResume() {
        super.onResume()
        UI.loadDisplay.visibility = View.GONE

        RecyclerSync.updateLocal()
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

    private fun launchEvaluationsView() {
        this.startActivity(
            Intent(this, EvaluationsActivity::class.java)
        )
    }

    private fun showHelpDialog() {
        // TODO: use Activity.infoDialog and show app version, author and usage (video?)
    }
}