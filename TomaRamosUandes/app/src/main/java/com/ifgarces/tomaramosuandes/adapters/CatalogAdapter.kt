package com.ifgarces.tomaramosuandes.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.toastf


class CatalogAdapter(private var data :List<Ramo>) : RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : CatalogViewHolder {
        return CatalogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.ramo_catalog_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder : CatalogViewHolder, position :Int) =
        holder.bind(this.data[position], position)

    public fun updateData(data :List<Ramo>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class CatalogViewHolder(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView    :View     = v // CardView
        private val cardContainer :View     = v.findViewById(R.id.ramoCatalog_container) // ConstraintLayout, inside `parentView`
        private val nombre        :TextView = v.findViewById(R.id.ramoCatalog_nombre)
        private val planEstudios  :TextView = v.findViewById(R.id.ramoCatalog_pe)
        private val materia       :TextView = v.findViewById(R.id.ramoCatalog_materia)
        private val NRC           :TextView = v.findViewById(R.id.ramoCatalog_NRC)
        private val secciónNum    :TextView = v.findViewById(R.id.ramoCatalog_seccion)

        fun bind(ramo :Ramo, position :Int) {
            this.nombre.text = ramo.nombre
            this.planEstudios.text = ramo.planEstudios
            this.materia.text = ramo.materia
            this.NRC.text = ramo.NRC.toString()
            this.secciónNum.text = ramo.sección

            /* setting card background color based on take status */
            if (ramo in DataMaster.getUserRamos()) {
                this.cardContainer.setBackgroundColor( this.parentView.context.getColor(R.color.catalog_taken) )
            } else {
                this.cardContainer.setBackgroundColor( this.parentView.context.getColor(R.color.catalog_untaken) )
            }

            /* setting `planEstudios` background color */
            if (ramo.planEstudios == "PE2016") {
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2016) )
            } else { // "PE2011", "PE2011/PE2016"
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2011) )
            }

            /* showing `Ramo` details and actions on click */
            parentView.setOnClickListener {
                // TODO: show detailed information about the `Ramo` and allow to take it. (BottomSheetDialog?)
                it.context.toastf("[DEBUG] '%s' clicked.", ramo.nombre)
            }
        }
    }
}