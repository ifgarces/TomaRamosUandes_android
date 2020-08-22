package com.ifgarces.tomaramosuandes.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.RamoDialogFragment
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.IntentKeys


class RamoEventsExpandedAdapter(private var data :List<Ramo>) : RecyclerView.Adapter<RamoEventsExpandedAdapter.ExpandedEventVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : ExpandedEventVH {
        return ExpandedEventVH(
            LayoutInflater.from(parent.context).inflate(R.layout.ramo_event_expanded_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :ExpandedEventVH, position :Int) = holder.bind(this.data[position], position)

    fun updateData(data :List<Ramo>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class ExpandedEventVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView     :View         = v
        private val ramoName       :TextView     = v.findViewById(R.id.ramoEventExp_ramo)
        private val eventsRecycler :RecyclerView = v.findViewById(R.id.ramoEventExp_recycler)
    
        fun bind(ramo :Ramo, position :Int) {
            this.ramoName.text = ramo.nombre
            this.eventsRecycler.layoutManager = LinearLayoutManager(parentView.context!!, LinearLayoutManager.HORIZONTAL, false)
            this.eventsRecycler.adapter = RamoEventsAdapter(data=/*...*/)

            this.ramoName.setOnClickListener {
                this.parentView.isEnabled = false // preventing the dialog to be invoked more than one time if the user clicks two times (due load time, maybe)
                val helper :FragmentActivity = this.parentView.context as FragmentActivity
                helper.intent.putExtra(IntentKeys.RAMO_NRC, ramo.NRC)
                helper.intent.putExtra(IntentKeys.RAMO_IS_TAKEN, true)

                RamoDialogFragment.summon(
                    manager = helper.supportFragmentManager,
                    onDismiss = {
                        this.parentView.isEnabled = true
                        this@RamoEventsExpandedAdapter.notifyItemChanged(position)
                    }
                )
            }
        }
    }
}