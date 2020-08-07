package com.ifgarces.tomaramosuandes

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ifgarces.tomaramosuandes.models.Curso
import com.ifgarces.tomaramosuandes.utils.toastf


class CatalogAdapter(private var data :List<Curso>) : RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : CatalogViewHolder {
        return CatalogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.curso_catalog_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :CatalogViewHolder, position :Int) =
        holder.bind(this.data[position], position)

    public fun updateData(data :List<Curso>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class CatalogViewHolder(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView    :View     = v // CardView
        private val cardContainer :View     = v.findViewById(R.id.cursoCatalog_container) // ConstraintLayout, inside `parentView`
        private val nombre        :TextView = v.findViewById(R.id.cursoCatalog_nombre)
        private val planEstudios  :TextView = v.findViewById(R.id.cursoCatalog_pe)
        private val materia       :TextView = v.findViewById(R.id.cursoCatalog_materia)
        private val NRC           :TextView = v.findViewById(R.id.cursoCatalog_NRC)
        private val secciónNum    :TextView = v.findViewById(R.id.cursoCatalog_seccionNum)

        fun bind(cc :Curso, position :Int) {
            this.nombre.text = cc.nombre
            this.planEstudios.text = cc.planEstudios
            this.materia.text = cc.materia
            this.NRC.text = cc.NRC.toString()
            this.secciónNum.text = cc.secciónNum

            /* setting card background color based on take status */
            if (cc in DataMaster.getUserCursos()) {
                this.cardContainer.setBackgroundColor( this.parentView.context.getColor(R.color.catalog_taken) )
            } else {
                this.cardContainer.setBackgroundColor( this.parentView.context.getColor(R.color.catalog_untaken) )
            }

            /* setting `planEstudios` background color */
            if (cc.planEstudios == "PE2016") {
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2016) )
            } else { // "PE2011", "PE2011/PE2016"
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2011) )
            }

            /* showing `Curso` details and actions on click */
            parentView.setOnClickListener {
                // TODO: show detailed information about the `Curso` and allow to take it. (BottomSheetDialog?)
                it.context.toastf("[DEBUG] '%s' clicked.", cc.nombre)
            }
        }
    }
}