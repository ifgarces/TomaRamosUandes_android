package com.ifgarces.tomaramosuandes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ifgarces.tomaramosuandes.models.AppMetadata
import com.ifgarces.tomaramosuandes.utils.infoDialog
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val SWAP_ACTIVITY_TIMER :Long = 1 // tiempo por el cual se muestra (segundos)

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        AppMetadata.init(activity=this)
        WebManager.init()
        DataMaster.init(
            activity = this,
            clear_database = false,
            onSuccess = {
                this.startActivity(
                    Intent(this@MainActivity, HomeActivity::class.java)
                )
            },
            onInternetError = {
                this.runOnUiThread {
                    this.infoDialog(
                        title = "Error",
                        message = "No se pudo obtener correctamente el catálogo de ramos. Revise su conexión a internet e intente más tarde.",
                        onDismiss = {
                            this.finish()
                        }
                    )
                }
            }
        )
    }
}