package com.ifgarces.tomaramosuandes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import com.ifgarces.tomaramosuandes.models.AppMetadata
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineFormat


/**
 * Waits for the initialization of all data and then, if successfull, launches `HomeActivity`.
 * If error, prompts dialog and terminates the program.
 * @property PROGRESSBAR_SPAWN_TIMEOUT The amount of milliseconds before the ProgressBar shows in front of the app icon.
 */
class MainActivity : AppCompatActivity() {

    private val PROGRESSBAR_SPAWN_TIMEOUT :Long = 2*1000

    private object UI {
        lateinit var loadLayer :View
        lateinit var loadBar   :ProgressBar

        fun init(owner :AppCompatActivity) {
            this.loadLayer = owner.findViewById(R.id.main_loadFocusLayer)
            this.loadBar = owner.findViewById(R.id.main_progressBar)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
        UI.init(owner=this)

        UI.loadLayer.visibility = View.GONE
        UI.loadBar.visibility = View.GONE

        UI.loadLayer.postDelayed({
            UI.loadLayer.visibility = View.VISIBLE
            UI.loadBar.visibility = View.VISIBLE
        }, PROGRESSBAR_SPAWN_TIMEOUT)

        AppMetadata.init(activity=this)
        WebManager.init()
        DataMaster.init(
            clear_database = false,
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
                        message = """No se pudo obtener correctamente el catálogo de ramos ${AppMetadata.getCatalogPeriod()}. \
                            Revise su conexión a internet e intente más tarde.""".multilineFormat(),
                        onDismiss = {
                            this.finish()
                        }
                    )
                }
            },
            onRoomError = {
                this.runOnUiThread {
                    this.infoDialog(
                        title = "Error",
                        message = """Se encontraron datos de ramos guardados por el usuario, pero no se pudieron cargar. \
                            Los datos están dañados o no compatibles con esta versión del programa.""".multilineFormat(),
                        onDismiss = {
                            // TODO: clear user data
                        }
                    )
                }
            }
        )
    }
}