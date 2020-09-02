package com.ifgarces.tomaramosuandes.utils

import android.content.Context
import com.ifgarces.tomaramosuandes.R


/**
 * This is a tricky mockery to miUandes app and its legendary-never-patched strange behaviors-bugs-things.
 * Yes. Very yes. All of the yes.
 */
fun showJokeMiUandesAppDialog(context :Context) {
    context.infoDialog(
        title = "miError",
        message = "Las credenciales han expirado. Por favor ingrese nuevamente.",
        onDismiss = {
            context.infoDialog(
                title = "😂",
                message = "¡No es cierto! Qué cosa más tonta, ¿verdad?"
            )
        },
        icon = R.drawable.alert_icon
    )
}