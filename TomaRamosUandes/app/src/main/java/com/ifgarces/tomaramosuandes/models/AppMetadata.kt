package com.ifgarces.tomaramosuandes.models


/**
 * Encapsulates metadata values for the app itself. Fetched from Firebase.
 * @property latestVersionName Latest available version of the app itself (e.g. "2021-10.1"). For
 * checking for updates.
 * @property catalogCurrentPeriod Latest catalog period (e.g. "2021-10").
 * @property catalogLastUpdated Date in which the catalog was updated for the last time on Firebase.
 * Free format, won't be parsed to a date object.
 * @property TABLE_NAME Firebase table name (won't use Room local database for this data class).
 * @property USER_APP_URL The main user link of this project (shortened), where general information
 * and latest release build are available.
 * @property APK_DOWNLOAD_URL Used to download the latest app itself (direct link to APK file).
 * @property FEEDBACK_URL URL for the user to give feedback to the developer.
 */
data class AppMetadata(
    val latestVersionName    :String,
    val catalogCurrentPeriod :String,
    val catalogLastUpdated   :String
) {
    companion object {
        const val TABLE_NAME :String = "app_metadata"
        const val USER_APP_URL :String = "https://bit.ly/TomadorRamosUandes"
        const val APK_DOWNLOAD_URL :String = "https://miuandescl-my.sharepoint.com/:u:/g/personal/ifgarces_miuandes_cl/EfsoeVHvbiROs7H0oqP-sNcBtFxUfgqKKvdrXhmvltH9lA?e=Cm9VhL?download=1"
        const val FEEDBACK_URL :String = "https://forms.gle/jwa2FcPSMeEcFwcXA"
    }
}
