package com.ifgarces.tomaramosuandes.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.SpanishFormatter
import com.ifgarces.tomaramosuandes.utils.spanishUpperCase


class RamoEventAdapter(
    private var data          :List<RamoEvent>,
    private val dayOfWeekOnly :Boolean // true for tests/exams, false for other events
) : RecyclerView.Adapter<RamoEventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : EventViewHolder {
        return EventViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.ramo_event_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :EventViewHolder, position :Int) =
        holder.bind(this.data[position], position)

    fun updateData(data :List<RamoEvent>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class EventViewHolder(v :View) : RecyclerView.ViewHolder(v) {
        //private val parentView  :View     = v
        private val dateDisplay :TextView = v.findViewById(R.id.ramoEvent_when)
        private val ti          :TextView = v.findViewById(R.id.ramoEvent_ti)
        private val tf          :TextView = v.findViewById(R.id.ramoEvent_tf)

        fun bind(event :RamoEvent, position :Int) {
            this.ti.text = event.startTime.toString()
            this.tf.text = event.endTime.toString()
            if (dayOfWeekOnly) { // prueba
                this.dateDisplay.text = SpanishFormatter.dayOfWeek(event.day).spanishUpperCase()
            }
            else { // clase, ayudantía o laboratorio
                this.dateDisplay.text = SpanishFormatter.localDate(event.date!!)
            }
        }
    }
}