package com.ifgarces.tomaramosuandes.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.BuildConfig
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.adapters.CatalogRamosAdapter
import com.ifgarces.tomaramosuandes.models.AppMetadata
import com.ifgarces.tomaramosuandes.utils.DataMaster


/**
 * Displays inscribed user's `Ramo`s and allows navigating to the current catalog.
 */
class UserRamosFragment : Fragment() {

    private class FragmentUI(owner :View) {
        val catalogButton   :Button = owner.findViewById(R.id.userRamos_catalogButton)
        val ramosRecycler   :RecyclerView = owner.findViewById(R.id.userRamos_ramosRecycler)
        val creditosCounter :TextView = owner.findViewById(R.id.userRamos_creditosSum)
    }

    private lateinit var UI :FragmentUI

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(
            if (DataMaster.user_stats.nightModeOn) R.layout.night_fragment_user_ramos
            else R.layout.fragment_user_ramos,
            container, false
        )
        this.UI = FragmentUI(owner = fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            homeActivity.setBottomNavItemSelected(this::class)
            homeActivity.setTopToolbarValues(
                title = "Ver ramos",
                subtitle = "",
                onHelpClick = this::showHelp
            )

            UI.ramosRecycler.layoutManager = LinearLayoutManager(homeActivity)
            UI.ramosRecycler.adapter = CatalogRamosAdapter(
                data = DataMaster.user_ramos,
                colorizeInscribed = true
            )

            UI.catalogButton.setOnClickListener {
                homeActivity.navigator.toCatalog()
            }
        }

        return fragView
    }

    override fun onResume() {
        super.onResume()

        UI.creditosCounter.text = DataMaster.getUserCreditSum().toString()

        // Displaying empty `Ramo`s list notice, when needed
        if (UI.ramosRecycler.adapter!!.itemCount == 0) {
            UI.ramosRecycler.visibility = View.INVISIBLE
        } else {
            UI.ramosRecycler.visibility = View.VISIBLE
        }
    }


    /**
     * Prompts a dialog with overall information/help about the application itself.
     */
    private fun showHelp() {
        // Setting the custom view layout for the dialog and button behaviors
        (DataMaster.user_stats.nightModeOn).let { isNightModeOn :Boolean ->
            val diagView :View = this.layoutInflater.inflate(
                if (isNightModeOn) R.layout.night_about_app_dialog else R.layout.about_app_dialog,
                null
            )
            val diagBuilder :AlertDialog.Builder = AlertDialog.Builder(
                this.requireContext(),
                if (isNightModeOn) R.style.myNightDialogTheme else R.style.myDialogTheme
            )
                .setView(diagView)
                .setCancelable(true)
                .setPositiveButton("Ya") { dialog :DialogInterface, _ :Int ->
                    dialog.dismiss()
                }
                .setNegativeButton("Saber más") { dialog :DialogInterface, _ :Int ->
                    // Opens the public application user URL
                    this.startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse(AppMetadata.USER_APP_URL)
                        )
                    )
                    dialog.dismiss()
                }

            // Setting UI elements for the dialog
            //val appName    :TextView = diagView.findViewById(R.id.aboutDialog_appName) // unused for now
            val appVersion :TextView = diagView.findViewById(R.id.aboutDialog_appVersion)
            val appAuthor  :TextView = diagView.findViewById(R.id.aboutDialog_appAuthor)
            appVersion.text = "Versión: %s".format(BuildConfig.VERSION_NAME)
            appAuthor.text = "Autor: %s".format(this.getString(R.string.app_author))

            // Displaying
            diagBuilder.create().show()
        }
    }
}