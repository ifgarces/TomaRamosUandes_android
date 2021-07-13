package com.ifgarces.tomaramosuandes

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ifgarces.tomaramosuandes.utils.*


/**
 * Waits for the initialization of all catalog and user data and then, if successfull,
 * launches `HomeActivity`. If an error is triggered, prompts an info dialog and terminates the program.
 * @property PROGRESSBAR_SPAWN_TIMEOUT The amount of milliseconds before the ProgressBar appears in front of the app icon.
 */
class MainActivity : AppCompatActivity() {

    private val PROGRESSBAR_SPAWN_TIMEOUT :Long = 2*1000

    private class ActivityUI(owner :AppCompatActivity) {
        val loadScreen :View = owner.findViewById(R.id.main_loadScreen)
    }; private lateinit var UI :ActivityUI

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
        this.UI = ActivityUI(owner=this)

        UI.loadScreen.visibility = View.GONE

        Thread { // ActivityUI.loadScreen.postDelayed({ ...
            Thread.sleep(PROGRESSBAR_SPAWN_TIMEOUT)
            this.runOnUiThread {
                UI.loadScreen.visibility = View.VISIBLE
            }
        }.start()

        DataMaster.init(
            activity = this,
            clearDatabase = false, // [!] should be `false` on any release, don't forget!!
            onSuccess = {
                Logf("[MainActivity] DataMaster successfully initialized.")
                this.startActivity(
                    Intent(this@MainActivity, HomeActivity::class.java)
                )
            },
            onInternetError = {
                this.runOnUiThread {
                    this.infoDialog(
                        title = "Error al obtener catálogo",
                        message = """No se pudo obtener correctamente el catálogo de ramos ${this.getString(R.string.CATALOG_PERIOD)}. \
                            Revise su conexión a internet. Si el error persiste, prueba descargando la app \
                            nuevamente en ${WebManager.MAIN_APP_URL}""".multilineTrim(),
                        onDismiss = {
                            this.finish()
                        },
                        icon = R.drawable.alert_icon
                    )
                }
            },
            onRoomError = {
                this.runOnUiThread {
                    this.infoDialog(
                        title = "Error",
                        message = """
                            Se encontraron datos de ramos tomados por el usuario, pero no se pudieron cargar. \
                            Los datos están dañados o no compatibles con esta versión del programa.
                            """.multilineTrim(),
                        onDismiss = {
                            Logf("[MainActivity] Wiping out existing Room database to avoid this same error to repeat eternally when re-opening the app.")
                            DataMaster.clear()
                        },
                        icon = R.drawable.alert_icon
                    )
                }
            }
        )
    }
}