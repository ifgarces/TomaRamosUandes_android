package com.ifgarces.tomaramosuandes

import java.net.URL


object WebManager {
    private const val APP_METADATA_URL :String = "..."
    private const val APK_DOWNLOAD_URL :String = "..."
    private const val ONLINE_CSV_URL   :String = "https://sites.google.com/site/test156885/CAT%C3%81LOGO%20COMPLETO%20ING.csv?attredirects=0&d=1"

    fun init() {
        this.checkForUpdates()
    }

    private fun checkForUpdates() {
        // TODO: check for updates
    }

    /**
     * Tries to fetch the catalog CSV from the web.
     * @exception java.net.UnknownHostException internet on connection failure
     */
    public fun FetchOnlineDataCSV() : String {
        return URL(this.ONLINE_CSV_URL).readText(charset=Charsets.UTF_8)
    }
}