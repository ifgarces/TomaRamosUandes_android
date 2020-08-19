package com.ifgarces.tomaramosuandes.utils

import android.content.Context
import com.ifgarces.tomaramosuandes.R
import java.net.URL


object WebManager {
    private const val APP_METADATA_URL :String = "..."
    private const val APK_DOWNLOAD_URL :String = "..."
    private const val ONLINE_CSV_URL   :String = "https://sites.google.com/site/test156885/CAT%C3%81LOGO%20COMPLETO%20ING.csv?attredirects=0&d=1"

    fun init(context :Context) {
        // TODO: may change for just toast display, and show badge on `HomeActivity` toolbar, allowing to download the new update
        if (this.updateAvailable()) {
            context.yesNoDialog(
                title = "Actualización disponible",
                message = "Hay una nueva versión (<inserte nombre versión>) de esta app. ¿Ir al link de descarga ahora?",
                onYesClicked = { /* TODO: Open download url */ },
                onNoClicked = {},
                icon = R.drawable.exclamation_icon // <- might change to an emoji
            )
        }
    }

    private fun updateAvailable() : Boolean { // TODO: check for updates
        // return (this.fetchLastestAppVersion() != AppMetadata.getVersion())
        return false
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
//    private fun fetchLastestAppVersion() : String {
//        return URL(this.APP_METADATA_URL).readText(charset=Charsets.UTF_8)
//            .... // extract app version from webpage body
//    }
}