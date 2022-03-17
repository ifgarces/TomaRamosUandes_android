package com.ifgarces.tomaramosuandes.models

import android.graphics.drawable.Drawable


/**
 * For modeling links for the `DashboardFragment`'s useful links section.
 * @property image Drawable with the image for displaying.
 * @property name Display name for the link.
 * @property uri Target URI.
 */
data class PrettyHyperlink(
    val image :Drawable,
    val name  :String,
    val uri   :String
)
