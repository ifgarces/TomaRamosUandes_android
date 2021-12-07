package com.ifgarces.tomaramosuandes.models

import java.util.Date


/**
 * WIll be user on `WebManager` to get app metadata from Firebase, like the current period and
 * latest app version.
 */
data class AppMetadata(
    val latestVersionName :String,
    val catalogLastUpdated :Date
)
