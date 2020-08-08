package com.ifgarces.tomaramosuandes.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.Ramo


class TakenRamosAdapter(private var data :MutableList<Ramo>) : RecyclerView.Adapter<TakenRamosAdapter.TakenViewHolder>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : TakenViewHolder {
        return TakenViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.ramo_taken_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder : TakenViewHolder, position :Int) =
        holder.bind(this.data[position], position)

    public fun updateData(data :MutableList<Ramo>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class TakenViewHolder(v :View) : RecyclerView.ViewHolder(v) {
        private val parent_view  :View        = v
        private val nombre       :TextView    = v.findViewById(R.id.userRamo_nombre)
        private val materia      :TextView    = v.findViewById(R.id.userRamo_materia)
        private val NRC          :TextView    = v.findViewById(R.id.userRamo_NRC)
        private val secciónNum   :TextView    = v.findViewById(R.id.userRamo_seccion)
        private val deleteButton :ImageButton = v.findViewById(R.id.userRamo_delete)

        fun bind(cc :Ramo, position :Int) {
            this.nombre.text = cc.nombre
            this.materia.text = cc.materia
            this.NRC.text = cc.NRC.toString()
            this.secciónNum.text = cc.sección

            parent_view.setOnClickListener {
                // TODO: view details of the `Ramo`
            }
            deleteButton.setOnClickListener {
                this@TakenRamosAdapter.data.removeAt(position)
                this@TakenRamosAdapter.notifyItemRemoved(position)
            }
        }
    }
}