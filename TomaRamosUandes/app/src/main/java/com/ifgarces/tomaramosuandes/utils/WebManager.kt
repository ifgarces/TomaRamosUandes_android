package com.ifgarces.tomaramosuandes.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.utils.WebManager.APK_DOWNLOAD_URL
import com.ifgarces.tomaramosuandes.utils.WebManager.APP_LATEST_VERSION_URL
import com.ifgarces.tomaramosuandes.utils.WebManager.USER_APP_URL
import com.ifgarces.tomaramosuandes.utils.WebManager.ONLINE_CSV_URL
import com.ifgarces.tomaramosuandes.utils.WebManager.catalogPeriodName
import java.net.URL
import java.net.UnknownHostException


/**
 * Handles update check on app startup (on its `init()` method) and various calls for getting online
 * metadata from the current available `Ramo`s catalog, etc.
 * @property APP_LATEST_VERSION_URL Used to query the latest available version of the very same app
 * (direct link to TXT file).
 * @property APK_DOWNLOAD_URL Used to download the latest app itself (direct link to APK file).
 * @property ONLINE_CSV_URL Used to fetch the catalog data for this period (direct link to CSV file).
 * @property DEBUG_CSV_URL Used to test changes in CSV parsing, for instance, with a new CSV before
 * affecting the file available on `ONLINE_CSV_URL`: without affecting app users. Debugging only.
 * @property USER_APP_URL The main user link of this project, where general information and latest
 * build are available.
 * @property catalogPeriodName Just an auxiliar variable for not to fetch the latest catalog
 * version every time the user inicializes `CatalogActivity`, for instance. Will be e.g. "2021-20".
 * We don't simply use the splitted `fetchLastestAppVersionName` method, because eventually the
 * catalog will be updated before and indepenently from the app.
 */
object WebManager {
    private const val APP_LATEST_VERSION_URL     :String = "https://drive.google.com/uc?id=1QtTMK5gU-47tJI8kpcf-v1v5PQvmANQQ&export=download"
    private const val CATALOG_LATEST_VERSION_URL :String = "https://drive.google.com/uc?export=download&id=1-XkBsoSb4emf0pBq5i6w9zVXTWy0DsCa"
    private const val APK_DOWNLOAD_URL           :String = "https://drive.google.com/uc?id=1gogvbPvYdLbWYhXuaHhS9TFom5Us2Go0&export=download"
    private const val ONLINE_CSV_URL             :String = "https://drive.google.com/uc?id=1Bo0MLop1YRdkOSwGsM1c7WOAtoiK7JP_&export=download"
    private const val DEBUG_CSV_URL              :String = "https://drive.google.com/uc?export=download&id=1KHji8-rJ3FFQWMTPEpyDki-jkjGmGHPj"
    public  const val USER_APP_URL               :String = "https://bit.ly/TomadorRamosUandes"

    public lateinit var catalogPeriodName :String

    /**
     * [This function needs to be called on a separated thread]
     * Prompts a dialog if an update is available.
     */
    fun init(activity :Activity) {
        // TODO: implement a "Do not show again" or "Ignore this update" feature

        Logf("[WebManager] Checking for updates...")

        val currentVer :String
        val latestVer :String
        try {
            currentVer = activity.getString(R.string.APP_VERSION)
            latestVer = this.fetchLastestAppVersionName()
            this.catalogPeriodName = this.fetchOnlineCatalogVersion()
        }
        catch (e :UnknownHostException) {
            Logf("[WebManager] Internet connection error: couldn't get app version or catalog version")
            activity.runOnUiThread {
                activity.infoDialog(
                    title = "💀 Error de conexión a internet",
                    message = "No se pudo obtener información de los ramos actuales. Asegure su conexión a internet y reinicie la app.",
                    onDismiss = {
                        activity.finishAffinity()
                    }
                )
            }
            return
        }

        Logf("[WebManager] Current version is %s, latest version is %s", currentVer, latestVer)

        if (latestVer != currentVer) { // prompts update dialog
            activity.runOnUiThread {
                activity.yesNoDialog(
                    title = "Actualización disponible",
                    message = "Hay una nueva versión de esta app: %s.\n\n¿Ir al link de descarga ahora?".format(latestVer),
                    onYesClicked = { // opens APK download link in default browser
                        activity.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(this.APK_DOWNLOAD_URL))
                        )
                    },
                    icon = R.drawable.exclamation_icon
                )
            }
        }
    }

    /**
     * Gets the catalog CSV body from the web.
     * @exception java.net.UnknownHostException On internet connection failure.
     */
    public fun fetchCatalogCSV() :String {
        return URL(this.DEBUG_CSV_URL).readText(charset=Charsets.UTF_8) //! Do not use `DEBUG_CSV_URL` in any release, don't forget!
    }

    /**
    * Gets the latest available version of the app itself (e.g. "2021-10.1").
    * @exception java.net.UnknownHostException On internet connection failure.
    */
    private fun fetchLastestAppVersionName() :String {
        return URL(this.APP_LATEST_VERSION_URL).readText(charset=Charsets.UTF_8)
    }

    /**
     * Opens the main user link for the project.
     */
    public fun openAppUrl(activity :Activity) {
        activity.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(this.USER_APP_URL))
        )
    }

    /**
     * Fetches the version of the catalog available now on `ONLINE_CSV_URL` (e.g. "2021-20").
     * @exception java.net.UnknownHostException On internet connection failure.
     */
    private fun fetchOnlineCatalogVersion() :String {
        return URL(this.CATALOG_LATEST_VERSION_URL).readText(charset=Charsets.UTF_8)
    }
}
