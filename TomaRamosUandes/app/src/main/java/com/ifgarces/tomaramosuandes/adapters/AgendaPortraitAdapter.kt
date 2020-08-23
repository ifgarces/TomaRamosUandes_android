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
import com.ifgarces.tomaramosuandes.utils.SpanishStringer
import com.ifgarces.tomaramosuandes.utils.infoDialog


/* Adapter used to display events in the portrait agenda view */
class AgendaPortraitAdapter(private var data :List<RamoEvent>) : RecyclerView.Adapter<AgendaPortraitAdapter.EventCardVH>() {

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
        private val parentView :View     = v
        private val ramoName   :TextView = v.findViewById(R.id.agendaPblock_ramo)
        private val startTime  :TextView = v.findViewById(R.id.agendaPblock_ti)
        private val endTime    :TextView = v.findViewById(R.id.agendaPblock_tf)
        private val type       :TextView = v.findViewById(R.id.agendaPblock_type)
    
        fun bind(event :RamoEvent, position :Int) {
            this.ramoName.text = DataMaster.findRamo(NRC=event.ramoNRC)!!.nombre
            this.startTime.text = event.startTime.toString()
            this.endTime.text = event.endTime.toString()
            this.type.text = SpanishStringer.ramoEventType(eventType=event.type, shorten=true)

            /* colorizing background of event box */
            val backColor :Int? = when(event.type) {
                RamoEventType.CLAS -> { this.parentView.context.getColor(R.color.clas) }
                RamoEventType.AYUD -> { this.parentView.context.getColor(R.color.ayud) }
                RamoEventType.LABT, RamoEventType.TUTR -> { this.parentView.context.getColor(R.color.labt) }
                else -> { null }
            }
            this.type.setBackgroundColor(backColor!!) // exception if invalid event or evaluation event, which should not go here (agenda)

            /* colorizing hole card if the event is on conflict status */
            val conflicted :List<RamoEvent> = DataMaster.getConflictsOf(event)
            if (conflicted.count() > 0) { // colorizing conflicted events
                this.parentView.setBackgroundColor( this.parentView.context.getColor(R.color.conflict_background) )
            }

            /* displaying event details in simple dialog */
            this.parentView.setOnClickListener {
                this.parentView.context.infoDialog(
                    title = "Detalle de evento",
                    message = event.toLargeString()
                )
            }
        }
    }
}