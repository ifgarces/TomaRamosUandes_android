package com.ifgarces.tomaramosuandes

import android.app.Activity


object AppMetadata {
    private lateinit var name    :String; fun getName() =
        name
    private lateinit var version :String; fun getVersion() =
        version
    private lateinit var period  :String; fun getCatalogPeriod() =
        period

    public fun init(activity :Activity) {
        name = activity.getString(R.string.APP_NAME)
        version = activity.getString(R.string.APP_VERSION)
        period = activity.getString(R.string.CATALOG_PERIOD)
    }
}