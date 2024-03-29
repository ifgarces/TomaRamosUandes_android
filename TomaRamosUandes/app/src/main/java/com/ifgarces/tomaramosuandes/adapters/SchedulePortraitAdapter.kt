package com.ifgarces.tomaramosuandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.SpanishToStringOf
import com.ifgarces.tomaramosuandes.utils.infoDialog
import java.util.concurrent.Executors


/**
 * Adapter used to display events in the portrait schedule view.
 */
class SchedulePortraitAdapter(private var data :List<RamoEvent>) :
    RecyclerView.Adapter<SchedulePortraitAdapter.EventCardVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) :EventCardVH {
        return EventCardVH(
            LayoutInflater.from(parent.context).inflate(
                if (DataMaster.getUserStats().nightModeOn) R.layout.night_schedule_portrait_block
                else R.layout.schedule_portrait_block,
                parent, false
            )
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :EventCardVH, position :Int) =
        holder.bind(this.data[position], position)

    inner class EventCardVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView :View = v
        private val ramoName   :TextView = v.findViewById(R.id.schedulePblock_ramo)
        private val startTime  :TextView = v.findViewById(R.id.schedulePblock_ti)
        private val endTime    :TextView = v.findViewById(R.id.schedulePblock_tf)
        private val type       :TextView = v.findViewById(R.id.schedulePblock_type)
        private val location   :TextView = v.findViewById(R.id.schedulePblock_location)

        fun bind(event :RamoEvent, position :Int) {
            (this.parentView.context as HomeActivity).let { homeActivity :HomeActivity ->
                this.ramoName.text = DataMaster.findRamo(
                    NRC = event.ramoNRC, searchInUserList = true
                )!!.nombre
                this.startTime.text = event.startTime.toString()
                this.endTime.text = event.endTime.toString()
                this.type.text = SpanishToStringOf.ramoEventType(
                    eventType = event.type, shorten = true
                )
                this.location.text = event.location

                val isNightModeOn :Boolean = (DataMaster.getUserStats().nightModeOn)
                // Colorizing event box according to theme and event type
                if (isNightModeOn) this.type.setTextColor(
                    ContextCompat.getColor(homeActivity, R.color.nightDefaultForeground)
                )
                this.type.setBackgroundColor(
                    when (event.type) {
                        RamoEventType.CLAS -> homeActivity.getColor(
                            if (isNightModeOn) R.color.night_blockColor_clas
                            else R.color.blockColor_clas
                        )
                        RamoEventType.AYUD -> homeActivity.getColor(
                            if (isNightModeOn) R.color.night_blockColor_ayud
                            else R.color.blockColor_ayud
                        )
                        RamoEventType.LABT, RamoEventType.TUTR -> homeActivity.getColor(
                            if (isNightModeOn) R.color.night_blockColor_labt
                            else R.color.blockColor_labt
                        )
                        else -> throw Exception("Invalid/unexpected event type for %s".format(event)) // exception if invalid event or evaluation event, which should not go here (schedule)
                    }
                )

                // Colorizing hole card if the event is on conflict status, asyncronously, so UI
                // elements itself will load soon, as checking for conflicts may be a heavy task
                Executors.newSingleThreadExecutor().execute {
                    if (DataMaster.getConflictsOf(event).count() > 0) {
                        homeActivity.runOnUiThread {
                            this.parentView.setBackgroundColor(homeActivity.getColor(
                                if (isNightModeOn) R.color.night_conflict_background
                                else R.color.conflict_background
                            ))
                        }
                    }
                }

                // Displaying event details in simple dialog
                this.parentView.setOnClickListener {
                    homeActivity.infoDialog(
                        title = "Detalle de evento",
                        message = event.toLargeString()
                    )
                }
            }
        }
    }
}