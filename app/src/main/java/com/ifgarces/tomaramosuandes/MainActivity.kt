package com.ifgarces.tomaramosuandes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ifgarces.tomaramosuandes.models.AppMetadata


class MainActivity : AppCompatActivity() {
    private val SWAP_ACTIVITY_TIMER :Long = 2 // tiempo por el cual se muestra (segundos)

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        AppMetadata.init(activity=this)
        DataMaster.init(activity=this, clear_database=false)

        asyncWaitAndContinue()
    }

    private fun asyncWaitAndContinue() {
        // TODO: luego de un rato, lanzar activity principal
    }
}