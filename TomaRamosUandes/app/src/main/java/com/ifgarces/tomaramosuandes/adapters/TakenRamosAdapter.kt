package com.ifgarces.tomaramosuandes.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.RamoDialogFragment
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.IntentKeys


class TakenRamosAdapter(private var data :List<Ramo>) : RecyclerView.Adapter<TakenRamosAdapter.TakenVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : TakenVH {
        return TakenVH(
            LayoutInflater.from(parent.context).inflate(R.layout.ramo_usertaken_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :TakenVH, position :Int) = holder.bind(this.data[position], position)

    fun updateData(data :List<Ramo>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class TakenVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView    :View     = v // CardView
        private val indexDisplay  :TextView = v.findViewById(R.id.ramoTaken_index)
        private val nombre        :TextView = v.findViewById(R.id.ramoTaken_nombre)
        private val planEstudios  :TextView = v.findViewById(R.id.ramoTaken_pe)
        private val materia       :TextView = v.findViewById(R.id.ramoTaken_materia)
        private val NRC           :TextView = v.findViewById(R.id.ramoTaken_NRC)
        private val sección       :TextView = v.findViewById(R.id.ramoTaken_seccion)

        fun bind(ramo :Ramo, position :Int) {
            this.indexDisplay.text = (position+1).toString()
            this.nombre.text = ramo.nombre
            this.planEstudios.text = ramo.planEstudios
            this.materia.text = ramo.materia
            this.NRC.text = ramo.NRC.toString()
            this.sección.text = ramo.sección

            /* setting `planEstudios` background color */
            if (ramo.planEstudios == "PE2016") {
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2016) )
            } else { // "PE2011", "PE2011/PE2016"
                this.planEstudios.setTextColor( this.parentView.context.getColor(R.color.PE2011) )
            }

            /* showing `Ramo` details and actions on click */
            parentView.setOnClickListener {
                this.parentView.isEnabled = false // preventing the dialog to be invoked more than one time if the user clicks two times (due load time, maybe)

                val helper :FragmentActivity = this.parentView.context as FragmentActivity
                helper.intent.putExtra(IntentKeys.RAMO_NRC, ramo.NRC)
                helper.intent.putExtra(IntentKeys.RAMO_IS_TAKEN, true)

                RamoDialogFragment.summon(
                    manager = helper.supportFragmentManager,
                    onDismiss = {
                        this.parentView.isEnabled = true
                        this@TakenRamosAdapter.notifyItemChanged(position)
                    }
                )
            }
        }
    }
}