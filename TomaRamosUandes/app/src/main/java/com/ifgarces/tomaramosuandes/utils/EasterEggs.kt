package com.ifgarces.tomaramosuandes.utils

import android.content.Context
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.models.UserStats


/**
 * This is the container for some dumb secret(s) of the app.
 */
object EasterEggs {
    private const val JOKE_SHOW_PROBABILITY :Short = 5 // percent of chance to see the joke dialog on a "try". Must be in [0, 100]. Default: 5

    /**
     * This is a mockery to the miUandes app and its legendary-never-patched strange behaviors-bugs-things. Yes.
     * This function will execute a joke dialog with a very low probability and only if it was not
     * previusly executed since the app was installed (also won't be shown on the first run of the app).
     */
    public fun handleJokeDialog(context :Context) {
        val stats :UserStats = DataMaster.getUserStats()
        if (stats.firstRunOfApp || stats.jokeExecuted) return
        if ((0..100).random() < this.JOKE_SHOW_PROBABILITY) {
            context.infoDialog(
                title = " ",
                message = "Las credenciales han expirado. Por favor, vuelva a ingresar.",
                onDismiss = {
                    context.infoDialog(
                        title = "😂",
                        message = "¡No es cierto! Solo era una burla hacia el eterno error de la app miUandes."
                    )
                }
            )
            DataMaster.jokeDialogWasExecuted()
        }
    }
}