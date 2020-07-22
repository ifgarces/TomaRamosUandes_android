package com.ifgarces.tomaramosuandes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ifgarces.tomaramosuandes.models.AppMetadata
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val SWAP_ACTIVITY_TIMER :Long = 2 // tiempo por el cual se muestra (segundos)

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        AppMetadata.init(activity=this)
        WebManager.init()
        DataMaster.init(activity=this, clear_database=false)

        asyncWaitToHomeActivity()
    }

    /**
    * Luego de un tiempo, lanza la activity principal de la app. Así las cosas tienen tiempo de cargarse, etc.
    * */
    private fun asyncWaitToHomeActivity() {
        Executors.newSingleThreadScheduledExecutor().schedule({
            this@MainActivity.startActivity(
                Intent(this@MainActivity, HomeActivity::class.java)
            )
            this@MainActivity.finish()
        }, SWAP_ACTIVITY_TIMER, TimeUnit.SECONDS)
    }
}