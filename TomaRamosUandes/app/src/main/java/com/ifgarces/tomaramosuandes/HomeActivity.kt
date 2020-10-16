package com.ifgarces.tomaramosuandes

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.ifgarces.tomaramosuandes.adapters.CatalogRamosAdapter
import com.ifgarces.tomaramosuandes.utils.AppMetadata
import com.ifgarces.tomaramosuandes.utils.EasterEggs
import com.ifgarces.tomaramosuandes.utils.Logf


class HomeActivity : AppCompatActivity() {

    object RecyclerSync { // allows to update the recycler from another activity/fragment
        // TODO: make sure this is right
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
            val creditSum :Int = DataMaster.getUserCreditSum()
            UI.creditosCounter.text = creditSum.toString()

            /* adjusting UI if user has not inscribed any `Ramo` */
            if (creditSum == 0) {
                UI.ramosRecycler.visibility = View.INVISIBLE
                UI.agendaButton.isEnabled = false
                UI.evaluationsButton.isEnabled = false
            } else {
                UI.ramosRecycler.visibility = View.VISIBLE
                UI.agendaButton.isEnabled = true
                UI.evaluationsButton.isEnabled = true
            }

            /* warning if sum(créditos) is higher than Uandes' normal limits */
            val creditSumColor :Int =
                if (creditSum > 33) {
                    UI.creditosCounter.context.getColor(R.color.creditSum_bad)
                } else {
                    UI.creditosCounter.context.getColor(R.color.creditSum_ok)
                }
            UI.creditosCounter.setTextColor(creditSumColor)
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
            isAllInscribed = true
        )

        UI.topBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_help -> { this.showHelp() }
                else -> {
                    Logf("[HomeActivity] Warning: unknown toolbar element pressed (id=%d).", it.itemId)
                }
            }
            return@setOnMenuItemClickListener true
        }
        UI.catalogButton.setOnClickListener { this.launchCatalogView() }
        UI.agendaButton.setOnClickListener { this.launchAgendaView() }
        UI.evaluationsButton.setOnClickListener { this.launchEvaluationsView() }

        // handling joke dialog
        EasterEggs.handleJokeDialog(context=this)
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
        UI.loadDisplay.visibility = View.VISIBLE // this operation takes some time, so we display a loading screen
        this.startActivity(
            Intent(this, CatalogActivity::class.java)
        )
    }

    private fun launchAgendaView() {
        this.startActivity(
            Intent(this, AgendaPortraitActivity::class.java)
        )
    }

    private fun launchEvaluationsView() {
        this.startActivity(
            Intent(this, EvaluationsActivity::class.java)
        )
    }

    private fun showHelp() {

        // TODO: insert link to clear demo video in HTML

        val diagBuilder :AlertDialog.Builder = AlertDialog.Builder(this)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok) { dialog :DialogInterface, _ :Int ->
                dialog.dismiss()
            }
        val diagView :View = this.layoutInflater.inflate(R.layout.about_and_help_dialog, null)
        diagBuilder.setView(diagView)

        val diagWebView :WebView = diagView.findViewById(R.id.about_webView)
        diagWebView.loadData( // loading HTML from asset file
            this.assets.open("AboutAndHelp.html").bufferedReader().readText()
                .format(AppMetadata.getName(), AppMetadata.getVersion()),
            "text/html",
            "UTF-8") // loading HTML
        diagWebView.webViewClient = object : WebViewClient() { // handling hyperlinks. References: https://stackoverflow.com/a/6343852
            override fun shouldOverrideUrlLoading(view :WebView?, url :String?) : Boolean {
                if (url != null) {
                    view!!.context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    )
                    return true
                } else {
                    return false
                }
            }
        }

        diagBuilder.create().show()
    }
}