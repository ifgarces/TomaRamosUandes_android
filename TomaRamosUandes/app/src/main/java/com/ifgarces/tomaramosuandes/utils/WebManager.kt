package com.ifgarces.tomaramosuandes.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.ifgarces.tomaramosuandes.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.Executors


/**
 * Handles update check on app startup (on its `init()` method) and various calls for getting online
 * metadata from the current available `Ramo`s catalog, etc. This is performed by fetching files
 * stored in Google Drive.
 * @property APK_DOWNLOAD_URL Used to download the latest app itself (direct link to APK file).
 * @property USER_APP_URL The main user link of this project, where general information and latest
 * build are available.
 * @property catalogPeriodName Just an auxiliar variable for not to fetch the latest catalog
 * version every time the user inicializes `CatalogActivity`, for instance. Will be e.g. "2021-20".
 * We don't simply use the splitted `fetchLastestAppVersionName` method, because eventually the
 * catalog will be updated before and indepenently from the app.
 * @property catalogLastUpdatedDate Auxiliar variable for getting the content under
 * `CATALOG_UPDATE_DATE` when needed, without making the actual HTTP GET.
 */
object WebManager {
    private const val OFFLINE_MODE :Boolean = true // temporal

    public  const val USER_APP_URL     :String = "https://bit.ly/TomadorRamosUandes"
    private const val APK_DOWNLOAD_URL :String = "https://drive.google.com/uc?id=1gogvbPvYdLbWYhXuaHhS9TFom5Us2Go0&export=download"

    private lateinit var catalogPeriodName :String
    public fun getCatalogLastPeriodName() = this.catalogPeriodName

    private lateinit var catalogLastUpdatedDate :String
    public fun getCatalogLastUpdateDate() = this.catalogLastUpdatedDate

    /**
     * Asynchronously checks for updates and prompts a proper dialog.
     * @param activity Caller activity, needed for executing some tasks.
     * @param onFinish Callback for when the initialization is complete.
     */
    fun init(activity :Activity, onFinish :(success :Boolean) -> Unit) {
        Executors.newSingleThreadExecutor().execute {
            // TODO: implement a "Do not show again" or "Ignore this update" feature

            Logf(this::class, "Checking for updates...")

            val currentVer :String = activity.getString(R.string.APP_VERSION)
            val latestVer :String
            try {
                if (OFFLINE_MODE) throw Exception("Intentional exception for offline mode") // for reaching the catch code
//                latestVer = this.fetchLastestAppVersionName()
                this.catalogPeriodName = this.fetchOnlineCatalogVersion()
                this.catalogLastUpdatedDate = this.fetchLatestCatalogUpdateDate()
            }
            catch (e :Exception) {
                Logf(this::class, "Internet connection error: couldn't get app version or catalog version. %s", e.stackTraceToString())
                if (e is UnknownHostException) {
                    Logf(this::class, "It was a common internet connection error, don't worry too much")
                }
                this.catalogPeriodName = activity.getString(R.string.APP_VERSION).split(".").first()
                this.catalogLastUpdatedDate = "offline"
                onFinish.invoke(false)
                return@execute
            }

//            Logf(this::class, "Current version is %s, latest version is %s", currentVer, latestVer)

//            if (latestVer != currentVer) { // prompts update dialog
//                activity.runOnUiThread {
//                    activity.yesNoDialog(
//                        title = "Actualización disponible",
//                        message = "Hay una nueva versión de esta app: %s.\n\n¿Ir al link de descarga ahora?".format(latestVer),
//                        onYesClicked = { // opens APK download link in default browser
//                            activity.startActivity(
//                                Intent(Intent.ACTION_VIEW, Uri.parse(this.USER_APP_URL))
//                            )
//                        },
//                        icon = R.drawable.exclamation_icon
//                    )
//                }
//            }
            onFinish.invoke(true)
        }
    }

    /**
     * Gets the catalog CSV body from the web.
     * @exception java.net.UnknownHostException On internet connection failure.
     * @exception java.io.FileNotFoundException This happened when Google Drive detected many
     * queries and blocked file(s), avoiding to use them in an app like this.
     */
    public fun fetchCatalogCSV(activity :Activity) :String {
        if (OFFLINE_MODE) {
            val reader :BufferedReader = BufferedReader(
                InputStreamReader(
                    activity.assets.open("catalog_offline.csv"),
                    "UTF-8"
                )
            )
            val lines :List<String> = reader.readLines()
            reader.close()
            return lines.joinToString("\n")
        }
        throw NotImplementedError()
//        return URL(this.DEBUG_CSV_URL).readText(charset=Charsets.UTF_8) //! Do not use `DEBUG_CSV_URL` in any release, don't forget!
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
     * @exception java.net.UnknownHostException On internet connection failure.3
     * @exception java.io.FileNotFoundException This happened when Google Drive detected many
     * queries and blocked file(s), avoiding to use them in an app like this.
     */
    private fun fetchOnlineCatalogVersion() :String {
        throw NotImplementedError()
//        return URL(this.CATALOG_LATEST_VERSION_URL).readText(charset=Charsets.UTF_8)
    }

    /**
     * Fetches the date the catalog was updated for the last time. Should be a "MM/dd/YYYY"
     * formatted string.
     * @exception java.net.UnknownHostException On internet connection failure.
     * @exception java.io.FileNotFoundException This happened when Google Drive detected many
     * queries and blocked file(s), avoiding to use them in an app like this.
     */
    private fun fetchLatestCatalogUpdateDate() :String {
        throw NotImplementedError()
//        return URL(this.CATALOG_UPDATE_DATE_URL).readText(charset=Charsets.UTF_8)
    }
}
