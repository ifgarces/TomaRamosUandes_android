package com.ifgarces.tomaramosuandes.models


/**
 * WIll be user on `WebManager` to get app metadata from Firebase, like the current period and
 * latest app version.
 * @property latestVersionName Latest available version of the app itself (e.g. "2021-10.1"). For
 * checking for updates.
 * @property catalogCurrentPeriod Latest catalog period (e.g. "2021-10").
 * @property catalogLastUpdated Date in which the catalog was updated for the last time on Firebase.
 * Format: `DateTimeFormatter.ISO_DATE`. As string, for avoiding problems with serialization on DB.
 * @property TABLE_NAME Firebase table name (won't use Room local database for this data class).
 * @property APK_DOWNLOAD_URL Used to download the latest app itself (direct link to APK file).
 * @property USER_APP_URL The main user link of this project, where general information and latest
 * build are available.
 */
data class AppMetadata(
    val latestVersionName :String,
    val catalogCurrentPeriod :String,
    val catalogLastUpdated :String
) {
    companion object {
        const val TABLE_NAME       :String = "app_metadata"
        const val USER_APP_URL     :String = "https://bit.ly/TomadorRamosUandes"
        const val APK_DOWNLOAD_URL :String = "https://drive.google.com/uc?id=1gogvbPvYdLbWYhXuaHhS9TFom5Us2Go0&export=download"
    }
}
