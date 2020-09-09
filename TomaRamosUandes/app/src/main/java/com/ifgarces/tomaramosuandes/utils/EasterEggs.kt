package com.ifgarces.tomaramosuandes.utils

import android.content.Context
import com.ifgarces.tomaramosuandes.DataMaster


object EasterEggs {
    private const val JOKE_SHOW_PROBABILITY :Int = 6 // percent of chance to see the joke dialog on a "try". Must be in [0, 100]

    /**
     * This is a mockery to miUandes app and its legendary-never-patched strange behaviors-bugs-things. Yes.
     * This function will execute a joke dialog with a very low probability and only if it was not
     * previusly executed since the app was installed (also won't be shown on the first app run).
     */
    public fun handleJokeDialog(context :Context) {
        if (! DataMaster.getUserStats().firstRunOfApp) {
            if ((0..100).random() <= this.JOKE_SHOW_PROBABILITY) {
                context.infoDialog(
                    title = " ",
                    message = "Las credenciales han expirado. Por favor, vuelva a ingresar.",
                    onDismiss = {
                        context.infoDialog(
                            title = "😂",
                            message = "¡No es cierto! Solo era una burla hacia el famoso error de la app miUandes."
                        )
                    }
                    //icon = R.drawable.alert_icon
                )
                DataMaster.jokeDialogWasExecuted()
            } else {
                Logf("[EasterEggs] Bad luck, joke dialog not executed this time.")
            }
        }
    }
}