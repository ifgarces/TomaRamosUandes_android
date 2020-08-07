package com.ifgarces.tomaramosuandes

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.ifgarces.tomaramosuandes.models.Curso


class TakenCursosAdapter(private var data :MutableList<Curso>) : RecyclerView.Adapter<TakenCursosAdapter.TakenViewHolder>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : TakenViewHolder {
        return TakenViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.curso_taken_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :TakenViewHolder, position :Int) =
        holder.bind(this.data[position], position)

    public fun updateData(data :MutableList<Curso>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class TakenViewHolder(v :View) : RecyclerView.ViewHolder(v) {
        private val parent_view  :View        = v
        private val nombre       :TextView    = v.findViewById(R.id.userCurso_nombre)
        private val materia      :TextView    = v.findViewById(R.id.userCurso_materia)
        private val NRC          :TextView    = v.findViewById(R.id.userCurso_NRC)
        private val secciónNum   :TextView    = v.findViewById(R.id.userCurso_seccionNum)
        private val deleteButton :ImageButton = v.findViewById(R.id.userCurso_delete)

        fun bind(cc :Curso, position :Int) {
            this.nombre.text = cc.nombre
            this.materia.text = cc.materia
            this.NRC.text = cc.NRC.toString()
            this.secciónNum.text = cc.secciónNum

            parent_view.setOnClickListener {
                // TODO: view details of the `Curso`
            }
            deleteButton.setOnClickListener {
                this@TakenCursosAdapter.data.removeAt(position)
                this@TakenCursosAdapter.notifyItemRemoved(position)
            }
        }
    }
}