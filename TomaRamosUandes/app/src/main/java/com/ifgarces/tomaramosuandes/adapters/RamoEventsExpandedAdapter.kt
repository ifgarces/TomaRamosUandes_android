package com.ifgarces.tomaramosuandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.RamoDialogFragment
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.IntentKeys


class RamoEventsExpandedAdapter(private var data :List<Ramo>) : RecyclerView.Adapter<RamoEventsExpandedAdapter.ExpandedEventVH>() {

    /**
     * Used to prevent the dialog from being invoked more than one time if the user clicks again
     * while the first one is still loading.
     */
    private object SingletonHelper {
        var isInstanceActive :Boolean = false
    }

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) : ExpandedEventVH {
        return ExpandedEventVH(
            LayoutInflater.from(parent.context).inflate(R.layout.ramo_with_evals_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :ExpandedEventVH, position :Int) = holder.bind(this.data[position], position)

    fun updateData(data :List<Ramo>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    inner class ExpandedEventVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView     :View         = v // MaterialCardView
        private val ramoName       :TextView     = v.findViewById(R.id.ramoEventExp_ramo)
        private val emptyMarker    :View         = v.findViewById(R.id.ramoEventExp_emptyMarker) // TextView
        private val eventsRecycler :RecyclerView = v.findViewById(R.id.ramoEventExp_recycler)
    
        fun bind(ramo :Ramo, position :Int) {
            this.ramoName.text = ramo.nombre

            /* deciding if to show empty recycler TextView or show the recycler */
            val recyclerData :List<RamoEvent> = DataMaster.getCatalogEvents().filter{ it.ramoNRC==ramo.NRC }
                .filter { it.isEvaluation() } // using this instead of roomDB should improve performance

            if (recyclerData.count() == 0) {
                this.emptyMarker.visibility = View.VISIBLE
                this.eventsRecycler.visibility = View.GONE
            } else {
                this.emptyMarker.visibility = View.GONE
                this.eventsRecycler.visibility = View.VISIBLE
                this.eventsRecycler.layoutManager = LinearLayoutManager(
                    parentView.context!!,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                this.eventsRecycler.adapter = RamoEventsAdapter(data=recyclerData, showEventType=true)
            }

            /* calling `Ramo` dialog card clicked */
            this.parentView.setOnClickListener {
                if (SingletonHelper.isInstanceActive) { return@setOnClickListener }
                SingletonHelper.isInstanceActive = true

                val helper :FragmentActivity = this.parentView.context as FragmentActivity
                helper.intent.putExtra(IntentKeys.RAMO_NRC, ramo.NRC)
                helper.intent.putExtra(IntentKeys.RAMO_IS_INSCRIBED, true)

                RamoDialogFragment.summon(
                    manager = helper.supportFragmentManager,
                    onDismiss = {
                        SingletonHelper.isInstanceActive = false
                        this@RamoEventsExpandedAdapter.notifyDataSetChanged()
                    }
                )
            }
        }
    }
}