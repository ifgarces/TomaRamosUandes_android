package com.ifgarces.tomaramosuandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import com.ifgarces.tomaramosuandes.utils.SpanishFormatter
import com.ifgarces.tomaramosuandes.utils.spanishUpperCase


class RamoEventsAdapter(private var data :List<RamoEvent>) : RecyclerView.Adapter<RamoEventsAdapter.EventVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : EventVH {
        return EventVH(
            LayoutInflater.from(parent.context).inflate(R.layout.ramo_event_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :EventVH, position :Int) = holder.bind(this.data[position], position)

    fun updateData(data :List<RamoEvent>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class EventVH(v :View) : RecyclerView.ViewHolder(v) {
        private val dayData :TextView = v.findViewById(R.id.ramoEvent_when)
        private val ti      :TextView = v.findViewById(R.id.ramoEvent_ti)
        private val tf      :TextView = v.findViewById(R.id.ramoEvent_tf)

        fun bind(event :RamoEvent, position :Int) {
            this.ti.text = event.startTime.toString()
            this.tf.text = event.endTime.toString()
            if (event.type == RamoEventType.PRBA || event.type == RamoEventType.EXAM) { // evaluación
                this.dayData.text = SpanishFormatter.localDate(event.date!!) // e.g. "18/11/2020"
            }
            else { // clase, ayudantía o laboratorio
                this.dayData.text = SpanishFormatter.dayOfWeek(event.dayofWeek).spanishUpperCase() // e.g. "viernes"
            }
        }
    }
}