package com.ifgarces.tomaramosuandes.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.ifgarces.tomaramosuandes.R
import java.net.URL


/**
 * Handles update check on app startup (on its `init()` method).
 * @property APP_LASTEST_VERSION_URL Used to query the lastest available version of the very same app (direct link to TXT file).
 * @property APK_DOWNLOAD_URL Used to download the lastest app itself (direct link to APK file).
 * @property ONLINE_CSV_URL Used to fetch the catalog data for this period (direct link to CSV file).
 */
object WebManager {
    private const val APP_LASTEST_VERSION_URL  :String = "https://drive.google.com/uc?id=1QtTMK5gU-47tJI8kpcf-v1v5PQvmANQQ&export=download"
    private const val APK_DOWNLOAD_URL         :String = "https://drive.google.com/uc?id=1gogvbPvYdLbWYhXuaHhS9TFom5Us2Go0&export=download"
    private const val ONLINE_CSV_URL           :String = "https://drive.google.com/uc?id=1Bo0MLop1YRdkOSwGsM1c7WOAtoiK7JP_&export=download"

    /**
     * [This function needs to be called on a separated thread]
     * Prompts a dialog if an update is available.
     */
    fun init(activity :Activity) {
        // TODO: implement a "Do not show again" or "Ignore this update" feature

        Logf("Checking for updates...")

        val currentVer :String = activity.getString(R.string.APP_VERSION)
        val lastestVer :String = this.fetchLastestAppVersionName()
        Logf("Current version: %s, lastest version: %s", currentVer, lastestVer)

        if (lastestVer != currentVer) { // prompts update dialog
            activity.runOnUiThread {
                activity.yesNoDialog(
                    title = "Actualización disponible",
                    message = "Hay una nueva versión de esta app: %s.\n\n¿Ir al link de descarga ahora?".format(lastestVer),
                    onYesClicked = { // opens download link in default browser
                        activity.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(this.APK_DOWNLOAD_URL))
                        )
                    },
                    onNoClicked = {},
                    icon = R.drawable.exclamation_icon
                )
            }
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
    private fun fetchLastestAppVersionName() : String {
        return URL(this.APP_LASTEST_VERSION_URL).readText(charset=Charsets.UTF_8)
    }
}