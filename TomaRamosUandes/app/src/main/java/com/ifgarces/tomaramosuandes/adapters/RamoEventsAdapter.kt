package com.ifgarces.tomaramosuandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.SpanishToStringOf
import com.ifgarces.tomaramosuandes.utils.spanishUpperCase
import org.w3c.dom.Text


class RamoEventsAdapter(
    private var data          :List<RamoEvent>,
    private val showEventType :Boolean
) : RecyclerView.Adapter<RamoEventsAdapter.EventVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : EventVH {
        return EventVH(
            LayoutInflater.from(parent.context).inflate(R.layout.ramoevent_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :EventVH, position :Int) = holder.bind(this.data[position], position)

    fun updateData(data :List<RamoEvent>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class EventVH(v :View) : RecyclerView.ViewHolder(v) {
        private val dayData  :TextView = v.findViewById(R.id.ramoEvent_when)
        private val ti       :TextView = v.findViewById(R.id.ramoEvent_ti)
        private val tf       :TextView = v.findViewById(R.id.ramoEvent_tf)
        private val evType   :TextView = v.findViewById(R.id.ramoEvent_type)
        private val location :TextView = v.findViewById(R.id.ramoEvent_location)

        fun bind(event :RamoEvent, position :Int) {
            this.ti.text = event.startTime.toString()
            this.tf.text = event.endTime.toString()
            this.location.text = "Sala: %s".format(event.location)

            /* deciding if to show date or week day */
            if (event.isEvaluation()) { // evaluación
                this.dayData.text = SpanishToStringOf.localDate(event.date!!) // e.g. "18/11/2020"
            } else { // clase, ayudantía o laboratorio
                this.dayData.text = SpanishToStringOf.dayOfWeek(event.dayOfWeek).spanishUpperCase() // e.g. "viernes"
            }

            /* deciding if to show event type */
            if (this@RamoEventsAdapter.showEventType) {
                this.evType.visibility = View.VISIBLE
                this.evType.text = SpanishToStringOf.ramoEventType(eventType=event.type, shorten=false)
            } else {
                this.evType.visibility = View.GONE
            }
        }
    }
}