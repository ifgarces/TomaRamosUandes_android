package com.ifgarces.tomaramosuandes.models

import android.graphics.drawable.Drawable


/**
 * For the advices section on `DashboardFragment`.
 * @property title
 * @property uri Contains the advice title (name), image, and URI.
 * @property image Optional image for displaying along with the advice.
 * @property uri Optioal URI for performing an on-click behaviour.
 */
data class CareerAdvice(
    val title       :String,
    val description :String,
    val image       :Drawable?,
    val uri         :String?
)
