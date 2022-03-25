package com.ifgarces.tomaramosuandes.models

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.ifgarces.tomaramosuandes.R


/**
 * For the advices section on `DashboardFragment`.
 * @property title Advice display title. A short string.
 * @property uri Optioal URI for performing an on-click behaviour.
 * @property description The string got from the `descriptionStringId` resource ID parameter.
 * @property image The drawable got from the `imageDrawableId` resource ID parameter, if given. Null
 * otherwise.
 * @param descriptionStringId Contains the advice title (name), image, and URI. A string resource
 * ID.
 * @param imageDrawableId Optional image for displaying along with the advice. It is a drawable
 * resource ID.
 * @param context Auxiliar context.
 */
class CareerAdvice(
    val title           :String,
    val uri             :String?,
    descriptionStringId :Int,
    imageDrawableId     :Int?,
    context             :Context
) {
    public val description :String = context.getString(descriptionStringId)
    public val image :Drawable? =
        if (imageDrawableId != null) ContextCompat.getDrawable(context, imageDrawableId) else null

    companion object {
        /**
         * Returns the list of `CareerAdvice`s for `DashboardFragment`.
         */
        public fun getStaticAdvices(context :Context) = listOf(
            CareerAdvice(
                title = "Beneficios cuenta de Google Uandes",
                descriptionStringId = R.string.advice_GSuite,
                imageDrawableId = R.drawable.webicon_gsuite,
                uri = "https://drive.google.com",
                context = context
            ),
            CareerAdvice(
                title = "Office 365 y OneDrive",
                descriptionStringId = R.string.advice_OneDrive,
                imageDrawableId = R.drawable.webicon_office365,
                uri = "https://www.office.com",
                context = context
            ),
            CareerAdvice(
                title = "Acceso a software de JetBrains",
                descriptionStringId = R.string.advice_JetBrains,
                imageDrawableId = R.drawable.webitem_jetbrains,
                uri = "https://www.jetbrains.com",
                context = context
            ),
            CareerAdvice(
                title = "AutoDesk Fusion 360",
                descriptionStringId = R.string.advice_Autodesk,
                imageDrawableId = R.drawable.webicon_fusion360,
                uri = "https://www.autodesk.com",
                context = context
            ),
            CareerAdvice(
                title = "Resúmenes y material ING varios ⚙",
                descriptionStringId = R.string.advice_GreatAyuda,
                imageDrawableId = R.drawable.webicon_greatayuda,
                uri = "http://www.g-ayuda.net",
                context = context
            ),
            CareerAdvice(
                title = "Material académico CDI",
                descriptionStringId = R.string.advice_CdiFolder,
                imageDrawableId = R.drawable.webicon_books,
                uri = "https://drive.google.com/drive/folders/0B7fLfOr3MydSVGY5MGhJTHJUcGs?resourcekey=0-Nd0xQYNHlDUh4utj-K-9tA&usp=sharing",
                context = context
            ),
            CareerAdvice(
                title = "Instagram CDI",
                descriptionStringId = R.string.advice_CdiInstagram,
                imageDrawableId = R.drawable.webicon_cdi,
                uri = "https://www.instagram.com/cdiuandes/",
                context = context
            ),
            CareerAdvice(
                title = "SCI-HUB y Library Genesis 📑",
                descriptionStringId = R.string.advice_SciHub,
                imageDrawableId = R.drawable.webicon_scihub,
                uri = "https://sci-hub.hkvisa.net",
                context = context
            ),
            CareerAdvice(
                title = "Sincronización de archivos importantes con la nube ☁",
                descriptionStringId = R.string.advice_FolderCloudSync,
                imageDrawableId = R.drawable.cloud_sync_icon,
                uri = null,
                context = context
            ),
            CareerAdvice(
                title = "CamScanner 📷",
                descriptionStringId = R.string.advice_CamScanner,
                imageDrawableId = R.drawable.webicon_camscanner,
                uri = "https://play.google.com/store/apps/details?id=com.intsig.lic.camscanner",
                context = context
            ),
            CareerAdvice(
                title = "WolframAlpha",
                descriptionStringId = R.string.advice_WolframAlpha,
                imageDrawableId = R.drawable.webicon_wolframalpha,
                uri = "https://www.wolframalpha.com",
                context = context
            ),
            CareerAdvice(
                title = "Symbolab",
                descriptionStringId = R.string.advice_Symbolab,
                imageDrawableId = R.drawable.webicon_symbolab,
                uri = "https://es.symbolab.com",
                context = context
            ),
            CareerAdvice(
                title = "MyBib 📚",
                descriptionStringId = R.string.advice_MyBib,
                imageDrawableId = R.drawable.webicon_mybib,
                uri = "https://www.mybib.com",
                context = context
            ),
            CareerAdvice(
                title = "draw.io",
                descriptionStringId = R.string.advice_DrawIo,
                imageDrawableId = R.drawable.webicon_drawio,
                uri = "https://draw.io",
                context = context
            ),
            CareerAdvice(
                title = "DroidCam",
                descriptionStringId = R.string.advice_DroidCam,
                imageDrawableId = R.drawable.webicon_droidcam,
                uri = "https://play.google.com/store/apps/details?id=com.dev47apps.droidcam",
                context = context
            ),
            CareerAdvice(
                title = "NightEye 🌙",
                descriptionStringId = R.string.advice_NightEye,
                imageDrawableId = R.drawable.webicon_nighteye,
                uri = "https://nighteye.app",
                context = context
            )
        )
    }
}
