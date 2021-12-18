package com.ifgarces.tomaramosuandes.models


/**
 * WIll be user on `WebManager` to get app metadata from Firebase, like the current period and
 * latest app version.
 * @property latestVersionName Latest available version of the app itself (e.g. "2021-10.1"). For
 * checking for updates.
 * @property catalogCurrentPeriod Latest catalog period (e.g. "2021-10").
 * @property catalogLastUpdated Date in which the catalog was updated for the last time on Firebase.
 * Format: `DateTimeFormatter.ISO_DATE`. As string, for avoiding problems with serialization on DB.
 */
data class AppMetadata(
    val latestVersionName :String,
    val catalogCurrentPeriod :String,
    val catalogLastUpdated :String
) {
    companion object {
        const val TABLE_NAME = "app_metadata" // for Firebase only
    }
}
