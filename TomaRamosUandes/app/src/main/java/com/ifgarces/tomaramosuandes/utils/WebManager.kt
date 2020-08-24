package com.ifgarces.tomaramosuandes.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ifgarces.tomaramosuandes.R
import java.net.URL


/**
 * Handles update check on app startup (with its `init()`).
 * @property APP_VERSION_URL Used to query the lastest available version of the very same app (direct link to TXT file).
 * @property APK_DOWNLOAD_URL Used to download the lastest app itself (direct link to APK file).
 * @property ONLINE_CSV_URL Used to fetch the catalog data for this period (direct link to CSV file).
 */
object WebManager {
    // the following two are temporal, testing if Google Drive and OneDrive support and mantain direct link
    private const val APP_VERSION_URL :String = "https://miuandescl-my.sharepoint.com/:t:/g/personal/ifgarces_miuandes_cl/EbHO0b2LtBdNtLsHEb_IHAQBKcv650WBk-iThCbViWenFQ?download=1"
    private const val APK_DOWNLOAD_URL :String = "https://drive.google.com/uc?id=1gogvbPvYdLbWYhXuaHhS9TFom5Us2Go0&export=download"

    // the following link corresponds to a file hosted in a Google Sites file cabinet. TODO: fix this mess!
    private const val ONLINE_CSV_URL   :String = "https://sites.google.com/site/test156885/CAT%C3%81LOGO%20COMPLETO%20ING.csv?attredirects=0&d=1"

    /**
     * This function must be called in a separate thread.
     */
    fun init(context :Context) {
        // TODO: may change for just toast display, and show badge on `HomeActivity` toolbar, allowing to download the new update
        // may bother if the user doesn't want to download and opens the app several times

        if (this.fetchLastestAppVersionName() != AppMetadata.getVersion()) { // prompts update dialog
            context.yesNoDialog(
                title = "Actualización disponible",
                message = "Hay una nueva versión de esta app: ((<inserte nombre versión>)).\n¿Ir al link de descarga ahora?",
                onYesClicked = { // opens download link in default browser
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(this.APK_DOWNLOAD_URL))
                    )
                },
                onNoClicked = {},
                icon = R.drawable.exclamation_icon // <- might change to a nice emoji
            )
        }
    }

    /**
     * Gets the catalog CSV body from the web.
     * @exception java.net.UnknownHostException On internet connection failure.
     */
    public fun fetchCatalogCSV() : String {
        return URL(this.ONLINE_CSV_URL).readText(charset=Charsets.UTF_8)
    }

    /**
    * Gets the lastest available version of the app itself.
    * @exception java.net.UnknownHostException On internet connection failure.
    */
    private fun fetchLastestAppVersionName() : String { // TODO: check for updates
        // TODO: get it.
        return AppMetadata.getVersion()
    }
}