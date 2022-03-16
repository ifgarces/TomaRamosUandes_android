package com.ifgarces.tomaramosuandes.adapters

import android.content.Context
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.SpanishToStringOf
import com.ifgarces.tomaramosuandes.utils.infoDialog
import java.time.LocalDate
import java.time.temporal.ChronoUnit


/**
 * For displaying a set of nearly incoming events for the user, on `DashboardFragment`.
 */
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
            val context :Context = this.parentView.context
            this.ti.text = event.startTime.toString()
            this.tf.text = event.endTime.toString()
            if (event.location != "") {
                this.location.text = "Sala: %s".format(event.location)
            } else {
                this.location.visibility = View.GONE
            }

            // Displaying string according to the time left for the event. Ref: https://discuss.kotlinlang.org/t/calculate-no-of-days-between-two-dates-kotlin/9826
            val daysLeft :Long = LocalDate.now().until(event.date!!, ChronoUnit.DAYS)
            if (daysLeft < 0) {
                Logf.warn(this::class, "Negative daysLeft for the event")
            }

            when {
                (daysLeft == 0L) -> {
                    this.dayData.text = "Hoy"
                    this.dayData.setTypeface(null, Typeface.BOLD)
                    this.dayData.setBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.holo_red_dark)
                    )
                }
                (daysLeft == 1L) -> {
                    this.dayData.text = "Mañana"
                    this.dayData.setTypeface(null, Typeface.BOLD)
                    this.dayData.setBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.holo_red_dark)
                    )
                }
                (daysLeft < 31L) -> {
                    this.dayData.text = "En %d días".format(daysLeft)
                }
                else -> {
                    this.dayData.text = "En más de 1 mes"
                    this.dayData.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.gray)

                    )
                }
            }

            // Event type
            this.evType.visibility = View.VISIBLE
            this.evType.text = SpanishToStringOf.ramoEventType(
                eventType = event.type, shorten = false
            )

            this.parentView.setOnClickListener {
                context.infoDialog(
                    title = "Detalle de evento",
                    message = event.toLargeString(),
                    onDismiss = {},
                    icon = null
                )
            }
        }
    }
}