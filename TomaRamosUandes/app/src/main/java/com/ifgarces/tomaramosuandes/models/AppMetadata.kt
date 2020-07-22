package com.ifgarces.tomaramosuandes.models

import android.app.Activity
import com.ifgarces.tomaramosuandes.R


object AppMetadata {
    private lateinit var APK_URL        :String; fun getDownloadURL() = this.APK_URL
    private lateinit var NAME           :String; fun getName() = this.NAME
    private lateinit var VERSION        :String; fun getVersion() = this.VERSION
    private lateinit var CATALOG_PERIOD :String; fun getCatalogPeriod() = this.CATALOG_PERIOD

    public fun init(activity :Activity) {
        this.NAME = activity.getString(R.string.APP_NAME)
        this.VERSION = activity.getString(R.string.APP_VERSION)
        this.CATALOG_PERIOD = activity.getString(R.string.CATALOG_PERIOD)
        this.APK_URL = "<insert APK download link>..."
    }
}