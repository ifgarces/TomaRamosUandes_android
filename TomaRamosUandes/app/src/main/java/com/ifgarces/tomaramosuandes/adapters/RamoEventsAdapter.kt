package com.ifgarces.tomaramosuandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.SpanishToStringOf
import com.ifgarces.tomaramosuandes.utils.spanishUpperCase


/**
 * For displaying `RamoEvent`s.
 * @property data The collection of items to display.
 * @property showEventType Whether to display a TextView indicating the type for each event. Useful
 * for when displaying different types (e.g. not for when displaying only evaluations).
 */
class RamoEventsAdapter(
    private var data :List<RamoEvent>,
    private val showEventType :Boolean
) : RecyclerView.Adapter<RamoEventsAdapter.EventVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) :EventVH {
        return EventVH(
            LayoutInflater.from(parent.context).inflate(
                if (DataMaster.user_stats.nightModeOn) R.layout.night_ramoevent_item
                else R.layout.ramoevent_item,
                parent, false
            )
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :EventVH, position :Int) =
        holder.bind(this.data[position], position)

    inner class EventVH(v :View) : RecyclerView.ViewHolder(v) {
        private val dayData  :TextView = v.findViewById(R.id.ramoEvent_when)
        private val ti       :TextView = v.findViewById(R.id.ramoEvent_ti)
        private val tf       :TextView = v.findViewById(R.id.ramoEvent_tf)
        private val evType   :TextView = v.findViewById(R.id.ramoEvent_type)
        private val location :TextView = v.findViewById(R.id.ramoEvent_location)

        fun bind(event :RamoEvent, position :Int) {
            this.ti.text = event.startTime.toString()
            this.tf.text = event.endTime.toString()
            if (event.location != "") {
                this.location.text = "Sala: %s".format(event.location)
            } else {
                this.location.text = "(sala no informada)"
                this.location.setTextColor(this.location.context.getColor(
                    if (DataMaster.user_stats.nightModeOn) R.color.nightDefaultForeground
                    else R.color.lightGray
                ))
            }

            // Deciding whether to show full date or just day of week
            if (event.isEvaluation()) { // evaluación
                this.dayData.text = SpanishToStringOf.localDate(event.date!!) // e.g. "18/11/2020"
            } else { // clase, ayudantía o laboratorio
                this.dayData.text = SpanishToStringOf.dayOfWeek(event.dayOfWeek).spanishUpperCase() // e.g. "viernes"
            }

            // Deciding whether to show event type or not
            if (this@RamoEventsAdapter.showEventType) {
                this.evType.visibility = View.VISIBLE
                this.evType.text = SpanishToStringOf.ramoEventType(
                    eventType = event.type, shorten = false
                )
            } else {
                this.evType.visibility = View.GONE
            }
        }
    }
}