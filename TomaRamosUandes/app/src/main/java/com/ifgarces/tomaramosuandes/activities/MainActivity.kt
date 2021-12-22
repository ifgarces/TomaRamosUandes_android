package com.ifgarces.tomaramosuandes.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.utils.*


/**
 * Waits for the initialization of all catalog and user data and then, if successfull, launches
 * `HomeActivity`. If an error is triggered, prompts an info dialog and terminates the application.
 * @property PROGRESSBAR_SPAWN_TIMEOUT The amount of seconds before the ProgressBar appears in front
 * of the app icon when it just starts.
 */
class MainActivity : AppCompatActivity() {

    private companion object {
        const val PROGRESSBAR_SPAWN_TIMEOUT :Long = 2
    }

    private class ActivityUI(owner :AppCompatActivity) {
        val loadScreen :View = owner.findViewById(R.id.main_loadScreen)
    }

    private lateinit var UI :ActivityUI

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
        this.UI = ActivityUI(owner = this)

        // Making sure Crashlytics is correctly configured (is this really needed?)
        try {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        } catch (e :Exception) { // catching, just in case
            Logf.error(this::class, "Could not configure Crashlytics: %s", e.stackTraceToString())
        }

        // Displaying loading screen when it takes more than `PROGRESSBAR_SPAWN_TIMEOUT` seconds to
        // initialize tha app
        UI.loadScreen.visibility = View.GONE
        Thread { // <==> UI.loadScreen.postDelayed({ ... }, PROGRESSBAR_SPAWN_TIMEOUT)
            Thread.sleep(PROGRESSBAR_SPAWN_TIMEOUT * 1000)
            this.runOnUiThread {
                UI.loadScreen.visibility = View.VISIBLE
            }
        }.start()

        // Initializing data: fetching latest catalog (Firebase) and loading local user data (Room)
        DataMaster.init(
            activity = this,
            clearDatabase = false, //! should be `false` on any release, don't forget!!
            forceLoadOfflineCSV = false, //! should be `false` on any release, don't forget!!
            onSuccess = {
                this.startActivity(
                    Intent(this@MainActivity, HomeActivity::class.java)
                )
            },
            onRoomError = {
                this.runOnUiThread {
                    this.infoDialog(
                        title = "Error",
                        message = """\
Se encontraron datos de ramos tomados por el usuario, pero no se pudieron cargar. \
Los datos están dañados o no compatibles con esta versión del programa.""".multilineTrim(),
                        onDismiss = {
                            Logf.debug(
                                this::class,
                                "Wiping out existing Room database to avoid this same error to repeat eternally when re-opening the app."
                            )
                            DataMaster.clear()
                        },
                        icon = R.drawable.alert_icon
                    )
                }
            },
            onFirebaseError = {
                // Nothing for now, as this dialog will appear on `HomeActivity` anyway, if there
                // are internet connection issues. Probably.
                //FirebaseMaster.showInternetConnectionErrorDialog(this)
            }
        )
    }
}