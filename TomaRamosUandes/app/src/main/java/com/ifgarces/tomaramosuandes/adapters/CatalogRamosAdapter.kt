package com.ifgarces.tomaramosuandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.RamoDialogFragment
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.IntentKeys


class CatalogRamosAdapter(
    private var data           :MutableList<Ramo>,
    private val isAllInscribed :Boolean // indicates whether all ramos in `data` are inscribed. If not, will highlight inscribed ones
) : RecyclerView.Adapter<CatalogRamosAdapter.CatalogVH>() {

    private object SingletonHelper {
        // used to prevent the dialog from being invoked more than once if user clicks again while still loading from first click
        var isInstanceActive :Boolean = false
    }

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : CatalogVH {
        return CatalogVH(
            LayoutInflater.from(parent.context).inflate(R.layout.ramo_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :CatalogVH, position :Int) = holder.bind(this.data[position], position)

    public fun updateData(data :MutableList<Ramo>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class CatalogVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView   :View     = v // CardView
        private val nombre       :TextView = v.findViewById(R.id.ramoCatalog_nombre)
        private val planEstudios :TextView = v.findViewById(R.id.ramoCatalog_pe)
        private val materia      :TextView = v.findViewById(R.id.ramoCatalog_materia)
        private val NRC          :TextView = v.findViewById(R.id.ramoCatalog_NRC)
        private val sección      :TextView = v.findViewById(R.id.ramoCatalog_seccion)

        fun bind(ramo :Ramo, position :Int) {
            this.nombre.text = ramo.nombre
            this.planEstudios.text = ramo.planEstudios
            this.materia.text = ramo.materia
            this.NRC.text = ramo.NRC.toString()
            this.sección.text = ramo.sección

            // Setting card background color based on inscribe status
            if (! this@CatalogRamosAdapter.isAllInscribed) { // distinguishing between inscribed and uninscribed (in catalog)
                if (ramo in DataMaster.getUserRamos()) {
                    this.parentView.setBackgroundColor(this.parentView.context.getColor(R.color.catalog_inscribed))
                } else {
                    this.parentView.setBackgroundColor(this.parentView.context.getColor(R.color.catalog_unInscribed))
                }
            }

            // Setting `planEstudios` background color
            if (ramo.planEstudios == "PE2016") {
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2016) )
            } else { // "PE2011", "PE2011/PE2016"
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2011) )
            }

            // Calling `Ramo` dialog card clicked
            this.parentView.setOnClickListener {
                if (SingletonHelper.isInstanceActive) { return@setOnClickListener }
                SingletonHelper.isInstanceActive = true

                val helper :FragmentActivity = this.parentView.context as FragmentActivity
                helper.intent.putExtra(IntentKeys.RAMO_NRC, ramo.NRC)
                helper.intent.putExtra(IntentKeys.RAMO_IS_INSCRIBED, (ramo in DataMaster.getUserRamos()))

                RamoDialogFragment.summon(
                    manager = helper.supportFragmentManager,
                    onDismiss = {
                        SingletonHelper.isInstanceActive = false
                        this@CatalogRamosAdapter.notifyItemChanged(position)
                    }
                )
            }
        }
    }
}
