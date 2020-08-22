package com.ifgarces.tomaramosuandes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.ifgarces.tomaramosuandes.models.Ramo


class EvaluationsActivity : AppCompatActivity() {

    private object UI {
        lateinit var topBar             :MaterialToolbar
        lateinit var eventsRecycler     :RecyclerView
        lateinit var eventsExportButton :Button

        fun init(owner :AppCompatActivity) {
            this.topBar = owner.findViewById(R.id.evals_topbar)
            this.eventsRecycler = owner.findViewById(R.id.evals_eventsRecycler)
            this.eventsExportButton = owner.findViewById(R.id.evals_exportEventsButton)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_evaluations)
        UI.init(owner=this)

        /*DataMaster.getUserRamos().forEach { ramo :Ramo ->
            ramo.events.forEach {
                if (it.isEvaluation()) {

                }
            }
        }*/
    }
}