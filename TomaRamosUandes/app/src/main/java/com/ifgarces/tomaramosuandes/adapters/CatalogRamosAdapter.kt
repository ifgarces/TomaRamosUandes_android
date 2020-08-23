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
    private var data     :MutableList<Ramo>,
    private val allTaken :Boolean // indicates if all ramos in `data` are taken. Also, won't recolorize them.
) : RecyclerView.Adapter<CatalogRamosAdapter.CatalogVH>() {

    private object SingletonHelper {
        // used to prevent the dialog from being invoked more than one time if the user clicks again while the first one is still loading
        var isDialogActive :Boolean = false
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
        private val parentView    :View     = v // CardView
        private val nombre        :TextView = v.findViewById(R.id.ramoCatalog_nombre)
        private val planEstudios  :TextView = v.findViewById(R.id.ramoCatalog_pe)
        private val materia       :TextView = v.findViewById(R.id.ramoCatalog_materia)
        private val NRC           :TextView = v.findViewById(R.id.ramoCatalog_NRC)
        private val sección       :TextView = v.findViewById(R.id.ramoCatalog_seccion)

        fun bind(ramo :Ramo, position :Int) {
            this.nombre.text = ramo.nombre
            this.planEstudios.text = ramo.planEstudios
            this.materia.text = ramo.materia
            this.NRC.text = ramo.NRC.toString()
            this.sección.text = ramo.sección

            /* setting card background color based on take status */
            if (! this@CatalogRamosAdapter.allTaken) { // distinguishing between taken and untaken (in catalog)
                if (ramo in DataMaster.getUserRamos()) {
                    this.parentView.setBackgroundColor(this.parentView.context.getColor(R.color.catalog_taken))
                } else {
                    this.parentView.setBackgroundColor(this.parentView.context.getColor(R.color.catalog_untaken))
                }
            }

            /* setting `planEstudios` background color */
            if (ramo.planEstudios == "PE2016") {
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2016) )
            } else { // "PE2011", "PE2011/PE2016"
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2011) )
            }

            /* calling `Ramo` dialog card clicked */
            this.parentView.setOnClickListener {
                if (SingletonHelper.isDialogActive) { return@setOnClickListener }
                SingletonHelper.isDialogActive = true

                val helper :FragmentActivity = this.parentView.context as FragmentActivity
                helper.intent.putExtra(IntentKeys.RAMO_NRC, ramo.NRC)
                helper.intent.putExtra(IntentKeys.RAMO_IS_TAKEN, (ramo in DataMaster.getUserRamos()))

                RamoDialogFragment.summon(
                    manager = helper.supportFragmentManager,
                    onDismiss = {
                        SingletonHelper.isDialogActive = false
                        this@CatalogRamosAdapter.notifyItemChanged(position)
                    }
                )
            }
        }
    }
}