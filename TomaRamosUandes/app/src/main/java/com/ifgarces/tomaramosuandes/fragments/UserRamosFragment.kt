package com.ifgarces.tomaramosuandes.fragments

import android.app.AlertDialog
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
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.HomeActivity
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.adapters.CatalogRamosAdapter
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.WebManager


/**
 * Displays inscribed user's `Ramo`s,
 */
class UserRamosFragment : Fragment() {
    
    private class FragmentUI(owner :View) {
        val catalogButton       :Button = owner.findViewById(R.id.userRamos_catalogButton)
        val ramosRecycler       :RecyclerView = owner.findViewById(R.id.userRamos_ramosRecycler)
        val emptyRecyclerMarker :TextView = owner.findViewById(R.id.userRamos_emptyRecyclerTextNotice)
        val creditosCounter     :TextView = owner.findViewById(R.id.userRamos_creditosSum)
    }

    private lateinit var UI :FragmentUI

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(R.layout.fragment_user_ramos, container, false)
        this.UI = FragmentUI(owner=fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            homeActivity.setTopToolbarValues(
                title = "Simulador toma de ramos Uandes",
                subtitle = "",
                onClick = {
                    this.showHelp()
                }
            )

            UI.ramosRecycler.layoutManager = LinearLayoutManager(homeActivity)
            UI.ramosRecycler.adapter = CatalogRamosAdapter(
                data = DataMaster.getUserRamos(),
                isAllInscribed = true
            )
            UI.ramosRecycler.layoutManager = null
            UI.ramosRecycler.adapter = null

            UI.catalogButton.setOnClickListener {
                //TODO: go to catalog fragment
                throw NotImplementedError()
            }

        }

        return fragView 
    }

    override fun onResume() {
        super.onResume()

        // Hiding loading screen overlay
        (this.requireActivity() as HomeActivity).hideLoadingScreen()

        // Dummy solution for issue #11, but doesn't seem to work...
        Logf("[UserRamosFragment] Updating recycler...")
        UI.ramosRecycler.let { recycler :RecyclerView ->
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
            recycler.layoutManager = LinearLayoutManager(this.requireContext())
            recycler.adapter!!.notifyDataSetChanged() // needed?
        }

        Logf("[UserRamosFragment] Current Ramos in recycler: %d", UI.ramosRecycler.adapter?.itemCount)

        UI.creditosCounter.text = DataMaster.getUserCreditSum().toString()

        // Displaying empty `Ramo`s list notice, when needed
        if (UI.ramosRecycler.adapter!!.itemCount == 0) {
            UI.ramosRecycler.visibility = View.INVISIBLE
        } else {
            UI.ramosRecycler.visibility = View.VISIBLE
        }
    }

    /**
     * Prompts a dialog with information/help about this view.
     */
    private fun showHelp() {
        val diagBuilder :AlertDialog.Builder = AlertDialog.Builder(this.requireContext(), R.style.myDialogTheme)
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
            "UTF-8"
        )
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