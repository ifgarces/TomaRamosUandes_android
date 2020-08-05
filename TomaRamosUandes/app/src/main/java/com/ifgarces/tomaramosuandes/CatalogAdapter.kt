package com.ifgarces.tomaramosuandes

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ifgarces.tomaramosuandes.models.Curso
import com.ifgarces.tomaramosuandes.utils.toastf


class CatalogAdapter(private var data :List<Curso>) : RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        return CatalogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.curso_catalog_item, parent, false)
        )
    }

    override fun getItemCount() = data.count()

    override fun onBindViewHolder(holder :CatalogViewHolder, position :Int) =
        holder.bind(data[position], position)

    fun updateData(data :List<Curso>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class CatalogViewHolder(v :View) : RecyclerView.ViewHolder(v) {
        private val parent_view  :View     = v
        private val context      :Context  = v.context
        private val nombre       :TextView = v.findViewById(R.id.cursoCatalog_nombre)
        private val planEstudios :TextView = v.findViewById(R.id.cursoCatalog_pe)
        private val materia      :TextView = v.findViewById(R.id.cursoCatalog_materia)
        private val NRC          :TextView = v.findViewById(R.id.cursoCatalog_NRC)
        private val secciónNum   :TextView = v.findViewById(R.id.cursoCatalog_seccionNum)

        fun bind(cc :Curso, position :Int) {
            this.nombre.text       = cc.nombre
            this.planEstudios.text = cc.planEstudios
            this.materia.text      = cc.materia
            this.NRC.text          = cc.NRC.toString()
            this.secciónNum.text   = cc.secciónNum

            if (cc.planEstudios == "PE2016") {
                this.planEstudios.setTextColor(this.context.getColor(R.color.PE2016))
            }
            else {
                this.planEstudios.setTextColor(this.context.getColor(R.color.PE2011))
            }

            parent_view.setOnClickListener {
                // TODO: show detailed information about the `Curso` and allow to take it.
                it.context.toastf("[DEBUG] '%s' clicked.", cc.nombre)
            }
        }
    }
}