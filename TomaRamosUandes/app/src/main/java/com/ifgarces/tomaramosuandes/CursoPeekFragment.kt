package com.ifgarces.tomaramosuandes

import android.app.Activity
import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView


class CursoPeekFragment : BottomSheetDialogFragment() {

    private object UI {
        lateinit var nombre     :TextView
        lateinit var NRC        :TextView
        lateinit var profe      :TextView
        lateinit var créditos   :TextView
        lateinit var materia    :TextView
        lateinit var cursoNum   :TextView
        lateinit var secciónNum :TextView
        lateinit var PE         :TextView
        lateinit var conectLiga :TextView
        lateinit var listaCruz  :TextView
        lateinit var clases     :RecyclerView
        lateinit var ayuds      :RecyclerView
        lateinit var labs       :RecyclerView
        lateinit var pruebas    :RecyclerView

        fun init(owner :Activity) {
            this.nombre = owner.findViewById(R.id.cursoPeek_nombre)
            this.NRC    = owner.findViewById(R.id.cursoPeek_NRC)
            // TODO: finish this
            // ...
            this.pruebas = owner.findViewById(R.id.cursoPeek_pruebasRecycler)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        // ...
    }

    override fun onAttach(context :Context) {
        super.onAttach(context)
        UI.init(owner=this.activity!!)
    }

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        return inflater.inflate(R.layout.fragment_curso_peek, container, false)
    }
}