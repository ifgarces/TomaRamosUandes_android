package com.ifgarces.tomaramosuandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.fragments.RamoDialogFragment
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.IntentKeys


/**
 * For displaying a collection of `Ramo`s.
 * @property data The collection of items to display.
 * @property colorizeInscribed Whether any inscribed `Ramo` being displayed should be highlighted in
 * another color. This makes sense in the catalog, but not in `UserRamosFragment`.
 */
class CatalogRamosAdapter(
    private var data :MutableList<Ramo>,
    private val colorizeInscribed :Boolean
) : RecyclerView.Adapter<CatalogRamosAdapter.CatalogVH>() {

    /**
     * Auxiliar object used to prevent the dialog from being invoked more than once if user clicks
     * again while still loading from first click. This is very unlikely to happen, but just in
     * case.
     */
    private object SingletonHelper {
        var isInstanceActive :Boolean = false
    }

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) :CatalogVH {
        return CatalogVH(
            LayoutInflater.from(parent.context).inflate(
                if (DataMaster.userStats.nightModeOn) R.layout.night_ramo_item
                else R.layout.ramo_item,
                parent, false
            )
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :CatalogVH, position :Int) =
        holder.bind(this.data[position], position)

    public fun updateData(data :MutableList<Ramo>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class CatalogVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView   :View = v // CardView
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

            (this.parentView.context as HomeActivity).let { homeActivity :HomeActivity ->
                val isInscribed :Boolean = ramo in DataMaster.userRamos

                // Setting card background color based on inscribe status
                if (!this@CatalogRamosAdapter.colorizeInscribed) {
                    this.parentView.setBackgroundColor(
                        homeActivity.getColor(
                            if (DataMaster.userStats.nightModeOn)
                                if (isInscribed) R.color.night_catalog_inscribed
                                else R.color.night_catalog_unInscribed
                            else
                                if (isInscribed) R.color.catalog_inscribed
                                else R.color.catalog_unInscribed
                        )
                    )
                }

                // Setting `planEstudios` background color
                this.planEstudios.setTextColor(
                    homeActivity.getColor(
                        if (DataMaster.userStats.nightModeOn)
                            if (ramo.planEstudios == "PE2016") R.color.nightPE2016
                            else R.color.nightPE2011
                        else
                            if (ramo.planEstudios == "PE2016") R.color.PE2016
                            else R.color.PE2011
                    )
                )

                // Calling `Ramo` dialog card clicked
                this.parentView.setOnClickListener {
                    if (SingletonHelper.isInstanceActive) {
                        return@setOnClickListener
                    }
                    SingletonHelper.isInstanceActive = true

                    homeActivity.intent.putExtra(IntentKeys.RAMO_NRC, ramo.NRC)
                    homeActivity.intent.putExtra(IntentKeys.RAMO_IS_INSCRIBED, isInscribed)

                    RamoDialogFragment(
                        onDismissAction = {
                            SingletonHelper.isInstanceActive = false
                            this@CatalogRamosAdapter.notifyDataSetChanged() //! not the most optimal way to do this...
                        }
                    ).show(homeActivity.supportFragmentManager, RamoDialogFragment::class.simpleName)
                }
            }
        }
    }
}
