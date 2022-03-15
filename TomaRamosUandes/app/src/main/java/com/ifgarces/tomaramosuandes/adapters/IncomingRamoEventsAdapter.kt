package com.ifgarces.tomaramosuandes.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.SpanishToStringOf
import com.ifgarces.tomaramosuandes.utils.spanishUpperCase
import java.time.LocalDate
import java.util.concurrent.TimeUnit


class IncomingRamoEventsAdapter(private val data :List<RamoEvent>) :
    RecyclerView.Adapter<IncomingRamoEventsAdapter.EventVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) :EventVH {
        return EventVH(
            LayoutInflater.from(parent.context).inflate(R.layout.ramoevent_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :EventVH, position :Int) =
        holder.bind(this.data[position], position)

    inner class EventVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView :View = v
        private val dayData    :TextView = v.findViewById(R.id.ramoEvent_when)
        private val ti         :TextView = v.findViewById(R.id.ramoEvent_ti)
        private val tf         :TextView = v.findViewById(R.id.ramoEvent_tf)
        private val evType     :TextView = v.findViewById(R.id.ramoEvent_type)
        private val location   :TextView = v.findViewById(R.id.ramoEvent_location)

        fun bind(event :RamoEvent, position :Int) {
            this.ti.text = event.startTime.toString()
            this.tf.text = event.endTime.toString()
            if (event.location != "") {
                this.location.text = "Sala: %s".format(event.location)
            } else {
                this.location.visibility = View.GONE
            }

            // Date and DayOfWeek
            val daysLeft :Long = TimeUnit.MILLISECONDS.toDays(
                event.date!!.toEpochDay() - LocalDate.now().toEpochDay()
            )

            this.dayData.text = when {
                (daysLeft == 0L) -> "Hoy"
                (daysLeft == 1L) -> "Mañana"
                (daysLeft < 31L) -> "En %d días".format(daysLeft)
                else -> "En más de 1 mes"
            }

            // Event type
            this.evType.visibility = View.VISIBLE
            this.evType.text = SpanishToStringOf.ramoEventType(
                eventType = event.type, shorten = false
            )

            this.parentView.setOnClickListener {
                //TODO: show details of the event in a dialog
                throw NotImplementedError()
            }
        }
    }
}