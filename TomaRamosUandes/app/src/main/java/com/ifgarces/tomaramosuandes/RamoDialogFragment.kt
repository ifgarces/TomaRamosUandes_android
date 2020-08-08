package com.ifgarces.tomaramosuandes

import android.app.Activity
import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ifgarces.tomaramosuandes.adapters.RamoEventAdapter
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.IntentKeys


class RamoDialogFragment : BottomSheetDialogFragment() {

    public object Mode { // open mode
        const val UNTAKEN :Int = 0 // `Ramo` can be taken and added to user's list
        const val TAKEN   :Int = 1 // `Ramo` is already in user's list
    }

    private object UI {
        lateinit var nombre       :TextView
        lateinit var NRC          :TextView
        lateinit var profe        :TextView
        lateinit var créditos     :TextView
        lateinit var materia      :TextView
        lateinit var curso        :TextView
        lateinit var sección      :TextView
        lateinit var PE           :TextView
        lateinit var conectLiga   :TextView
        lateinit var listaCruz    :TextView
        lateinit var clases       :RecyclerView
        lateinit var ayuds        :RecyclerView
        lateinit var labs         :RecyclerView
        lateinit var pruebas      :RecyclerView
        lateinit var actionButton :MaterialButton

        fun init(owner :Activity) {
            this.nombre       = owner.findViewById(R.id.ramoPeek_nombre)
            this.NRC          = owner.findViewById(R.id.ramoPeek_NRC)
            this.profe        = owner.findViewById(R.id.ramoPeek_profesor)
            this.créditos     = owner.findViewById(R.id.home_creditos)
            this.materia      = owner.findViewById(R.id.ramoPeek_materia)
            this.curso        = owner.findViewById(R.id.ramoPeek_curso)
            this.sección      = owner.findViewById(R.id.ramoPeek_seccion)
            this.PE           = owner.findViewById(R.id.ramoPeek_PE)
            this.conectLiga   = owner.findViewById(R.id.ramoPeek_liga)
            this.listaCruz    = owner.findViewById(R.id.ramoPeek_lCruz)
            this.clases       = owner.findViewById(R.id.ramoPeek_clasesRecycler)
            this.ayuds        = owner.findViewById(R.id.ramoPeek_ayudsRecycler)
            this.labs         = owner.findViewById(R.id.ramoPeek_labsRecycler)
            this.pruebas      = owner.findViewById(R.id.ramoPeek_pruebasRecycler)
            this.actionButton = owner.findViewById(R.id.ramoPeek_button)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        // ?
    }

    override fun onAttach(context :Context) {
        super.onAttach(context)
        UI.init(owner=this.activity!!)

        for ( recycler :RecyclerView in listOf(UI.clases, UI.ayuds, UI.labs, UI.pruebas) ) {
            recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        val ramoStatus :Int = this.activity!!.intent.getIntExtra(IntentKeys.PEEK_RAMO_MODE, -99999)
        val nrc  :Int = this.activity!!.intent.getIntExtra(IntentKeys.RAMO_NRC, -99999)
        val ramo :Ramo = DataMaster.findRamoByNRC(nrc)!!

        UI.nombre.text = ramo.nombre
        UI.créditos.text = ramo.créditos.toString()
        UI.materia.text = ramo.materia
        UI.curso.text = ramo.curso.toString()
        UI.sección.text = ramo.sección
        UI.PE.text = ramo.planEstudios
        UI.conectLiga.text = ramo.conectLiga
        UI.listaCruz.text = ramo.listaCruz

        UI.clases.adapter = RamoEventAdapter(data=ramo.clases, dayOfWeekOnly = false)

        if (ramoStatus == Mode.TAKEN) {
            UI.actionButton.icon = context.getDrawable(R.drawable.trash_icon)
            UI.actionButton.text = "Borrar ramo"
            UI.actionButton.setOnClickListener { this.deleteRamo() }
        }
        else {
            UI.actionButton.icon = context.getDrawable(R.drawable.add_icon)
            UI.actionButton.text = "Tomar ramo"
            UI.actionButton.setOnClickListener { this.takeRamo() }
        }
    }

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        return inflater.inflate(R.layout.fragment_ramo_peek, container, false)
    }

    private fun deleteRamo() {

    }

    private fun takeRamo() {

    }
}