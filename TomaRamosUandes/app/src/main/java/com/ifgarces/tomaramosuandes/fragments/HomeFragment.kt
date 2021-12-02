package com.ifgarces.tomaramosuandes.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.ifgarces.tomaramosuandes.AgendaPortraitActivity
import com.ifgarces.tomaramosuandes.CatalogActivity
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.EvaluationsActivity
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.adapters.CatalogRamosAdapter
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.WebManager
import java.util.concurrent.Executors


class HomeFragment : Fragment() {
    
    private class FragmentUI(owner :View) {
        val topBar              :MaterialToolbar = owner.findViewById(R.id.home_topbar)
        val catalogButton       :Button = owner.findViewById(R.id.home_catalogButton)
        val ramosRecycler       :RecyclerView = owner.findViewById(R.id.home_ramosRecycler)
        val emptyRecyclerMarker :TextView = owner.findViewById(R.id.home_emptyRecyclerText)
        val creditosCounter     :TextView = owner.findViewById(R.id.home_creditos)
        val agendaButton        :Button = owner.findViewById(R.id.home_agendaButton)
        val evaluationsButton   :Button = owner.findViewById(R.id.home_pruebasButton)
        //val loadDisplay         :View = owner.findViewById(R.id.home_loadScreen)
    }

    private lateinit var UI :FragmentUI

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View? = inflater.inflate(R.layout.fragment_home, container, false)
        
        this.UI = FragmentUI(owner=fragView!!)

        // Checking for updates here instead of MainAcivity, for simplicity
        Executors.newSingleThreadExecutor().execute {
            WebManager.init(activity=this.requireActivity())
        }

//        UI.ramosRecycler.layoutManager = LinearLayoutManager(this)
//        UI.ramosRecycler.adapter = CatalogRamosAdapter(
//            data = DataMaster.getUserRamos(),
//            isAllInscribed = true
//        )
        UI.ramosRecycler.layoutManager = null
        UI.ramosRecycler.adapter = null

        UI.topBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_help -> {
                    this.showHelp()
                }
                else -> {
                    Logf("[HomeActivity] Warning: unknown toolbar element pressed (titled %s).", it.title.toString())
                }
            }
            return@setOnMenuItemClickListener true
        }
        UI.catalogButton.setOnClickListener { this.launchCatalogView() }
        UI.agendaButton.setOnClickListener { this.launchAgendaView() }
        UI.evaluationsButton.setOnClickListener { this.launchEvaluationsView() }

        return fragView 
    }

    override fun onResume() {
        super.onResume()

        UI.loadDisplay.visibility = View.GONE

        Logf("[HomeActivity] Updating recycler...")
        UI.ramosRecycler.let { recycler :RecyclerView -> // dummy solution for issue #11, but don't seem to work
            recycler.adapter = null
            recycler.layoutManager = null
            recycler.recycledViewPool.clear()
            recycler.swapAdapter(
                CatalogRamosAdapter(
                    data = DataMaster.getUserRamos(),
                    isAllInscribed = true
                ),
                false
            )
            recycler.layoutManager = LinearLayoutManager(this)
            recycler.adapter!!.notifyDataSetChanged()
        }

        //(UI.ramosRecycler.adapter as CatalogRamosAdapter).notifyDataSetChanged()
        Logf("[HomeActivity] Current Ramos in recycler: %d", UI.ramosRecycler.adapter?.itemCount)

        UI.creditosCounter.text = DataMaster.getUserCreditSum().toString()

        // Displaying empty `Ramo`s list notice when needed
        if (UI.ramosRecycler.adapter?.itemCount == 0) {
            UI.ramosRecycler.visibility = View.INVISIBLE
            UI.agendaButton.isEnabled = false
            UI.evaluationsButton.isEnabled = false
        } else {
            UI.ramosRecycler.visibility = View.VISIBLE
            UI.agendaButton.isEnabled = true
            UI.evaluationsButton.isEnabled = true
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        this.finishAffinity()
//    }

    private fun launchCatalogView() {
        //TODO: replace this with a proper loading bar dialog, if that is easier
        //UI.loadDisplay.visibility = View.VISIBLE // this operation takes some time, so we display a loading screen
        this.startActivity(
            Intent(this.requireContext(), CatalogActivity::class.java)
        )
    }

    private fun launchAgendaView() {
        this.startActivity(
            Intent(this.requireContext(), AgendaPortraitActivity::class.java)
        )
    }

    private fun launchEvaluationsView() {
        this.startActivity(
            Intent(this.requireContext(), EvaluationsActivity::class.java)
        )
    }

    /**
     * Prompts a dialog with information/help about this view.
     */
    private fun showHelp() {
        // TODO: insert link of a clear demo video

        val diagBuilder :AlertDialog.Builder = AlertDialog.Builder(this, R.style.myDialogTheme)
            .setCancelable(true)
            .setPositiveButton("Cerrar") { dialog :DialogInterface, _ :Int ->
                dialog.dismiss()
            }
            .setNegativeButton("Saber más") { dialog :DialogInterface, _ :Int ->
                WebManager.openAppUrl(activity=this.requireActivity())
                dialog.dismiss()
            }
        val diagView :View = this.layoutInflater.inflate(R.layout.about_and_help_dialog, null)
        diagBuilder.setView(diagView)

        val diagWebView :WebView = diagView.findViewById(R.id.about_webView)
        diagWebView.loadData( // loading HTML from asset file
            this.requireActivity().assets.open("AboutAndHelp.html").bufferedReader().readText()
                .format(this.getString(R.string.APP_NAME), this.getString(R.string.APP_VERSION)),
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