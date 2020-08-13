package com.ifgarces.tomaramosuandes.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType


class RamoEventCardsAdapter(private var data :List<RamoEvent>) : RecyclerView.Adapter<RamoEventCardsAdapter.EventCardVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : EventCardVH {
        return EventCardVH(
            LayoutInflater.from(parent.context).inflate(R.layout.agenda_portrait_block, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :EventCardVH, position :Int) = holder.bind(this.data[position], position)

    fun updateData(data :List<RamoEvent>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class EventCardVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView :View = v
        private val ramo       :TextView = v.findViewById(R.id.agendaPblock_ramo)
        private val startTime  :TextView = v.findViewById(R.id.agendaPblock_ti)
        private val endTime    :TextView = v.findViewById(R.id.agendaPblock_tf)
        private val type       :TextView = v.findViewById(R.id.agendaPblock_type)
    
        fun bind(event :RamoEvent, position :Int) {
            this.ramo.text = DataMaster.findRamo(NRC=event.ramoNRC)!!.nombre
            this.startTime.text = event.startTime.toString()
            this.endTime.text = event.endTime.toString()
            this.type.text = RamoEventType.toStringOf(eventType=event.type)

            // colorize background of event box
            val color :Int? = when(event.type) {
                RamoEventType.CLAS -> { this.parentView.context.getColor(R.color.clas) }
                RamoEventType.AYUD -> { this.parentView.context.getColor(R.color.ayud) }
                RamoEventType.LABT, RamoEventType.TUTR -> { this.parentView.context.getColor(R.color.labt) }
                else -> { null }
            }
            this.type.setBackgroundColor(color!!) // exception if invalid event or evaluation event, which should not go here (agenda)



            parentView.setOnClickListener {
                // TODO: show dialog with details of the event
            }
        }
    }
}