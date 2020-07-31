package com.ifgarces.tomaramosuandes.models

import android.app.Activity
import com.ifgarces.tomaramosuandes.R


object AppMetadata {
    private lateinit var name    :String; fun getName() = this.name
    private lateinit var version :String; fun getVersion() = this.version
    private lateinit var period  :String; fun getCatalogPeriod() = this.period

    public fun init(activity :Activity) {
        this.name = activity.getString(R.string.APP_NAME)
        this.version = activity.getString(R.string.APP_VERSION)
        this.period = activity.getString(R.string.CATALOG_PERIOD)
    }
}