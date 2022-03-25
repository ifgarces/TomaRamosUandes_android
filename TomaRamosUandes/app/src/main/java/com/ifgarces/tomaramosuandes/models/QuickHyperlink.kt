package com.ifgarces.tomaramosuandes.models

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.ifgarces.tomaramosuandes.R


/**
 * For modeling links for the `DashboardFragment`'s useful links section.
 * @param imageDrawableId Drawable resource ID to display.
 * @property image Drawable with the image for displaying, got from the `imageDrawableId` parameter.
 * @property name Display name for the link.
 * @property uri Target URI.
 */
class QuickHyperlink(
    imageDrawableId :Int,
    val name  :String,
    val uri   :String,
    context :Context
) {
    public val image :Drawable = ContextCompat.getDrawable(context, imageDrawableId)!!

    companion object {
        public fun getStaticQuickLinks(context :Context) = listOf(
            QuickHyperlink(
                imageDrawableId = R.drawable.webicon_canvas,
                name = "Canvas",
                uri = "https://uandes.instructure.com",
                context = context
            ),
            QuickHyperlink(
                imageDrawableId = R.drawable.webicon_banner,
                name = "Banner MiUandes",
                uri = "https://mi.uandes.cl",
                context = context
            ),
            QuickHyperlink(
                imageDrawableId = R.drawable.uandes_logo_simple,
                name = "SAF",
                uri = "https://saf.uandes.cl/ing",
                context = context
            ),
            QuickHyperlink(
                imageDrawableId = R.drawable.webicon_academic_calendar,
                name = "Calendario académico Uandes",
                uri = "https://www.uandes.cl/alumnos/informacion-academica/calendario-academico",
                context = context
            ),
            QuickHyperlink(
                imageDrawableId = R.drawable.handshake_icon,
                name = "Apoyo académico Uandes",
                uri = "https://www.uandes.cl/alumnos/apoyo-academico",
                context = context
            )
        )
    }
}
