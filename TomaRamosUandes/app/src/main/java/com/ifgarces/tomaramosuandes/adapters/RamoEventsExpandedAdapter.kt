package com.ifgarces.tomaramosuandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.fragments.RamoDialogFragment
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.IntentKeys


class RamoEventsExpandedAdapter(private var data :List<Ramo>) :
    RecyclerView.Adapter<RamoEventsExpandedAdapter.ExpandedEventVH>() {

    /**
     * Used to prevent the dialog from being invoked more than one time if the user clicks again
     * while the first one is still loading.
     */
    private object SingletonHelper {
        var isInstanceActive :Boolean = false
    }

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) :ExpandedEventVH {
        return ExpandedEventVH(
            LayoutInflater.from(parent.context).inflate(
                if (DataMaster.userStats.nightModeOn) R.layout.night_ramo_with_evals_item
                else R.layout.ramo_with_evals_item,
                parent, false
            )
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :ExpandedEventVH, position :Int) =
        holder.bind(this.data[position], position)

    inner class ExpandedEventVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView     :View = v // MaterialCardView
        private val ramoName       :TextView = v.findViewById(R.id.ramoEventExp_ramo)
        private val emptyMarker    :View = v.findViewById(R.id.ramoEventExp_emptyMarker) // TextView
        private val eventsRecycler :RecyclerView = v.findViewById(R.id.ramoEventExp_recycler)

        fun bind(ramo :Ramo, position :Int) {
            this.ramoName.text = ramo.nombre

            (this.parentView.context as HomeActivity).let { homeActivity :HomeActivity ->
                val recyclerData :List<RamoEvent> =
                    DataMaster.catalogEvents.filter { it.ramoNRC == ramo.NRC }
                        .filter { it.isEvaluation() } // using this instead of roomDB should improve performance

                // Deciding if to show empty recycler TextView or show the recycler
                if (recyclerData.count() == 0) {
                    this.emptyMarker.visibility = View.VISIBLE
                    this.eventsRecycler.visibility = View.GONE
                } else {
                    this.emptyMarker.visibility = View.GONE
                    this.eventsRecycler.visibility = View.VISIBLE
                    this.eventsRecycler.layoutManager = LinearLayoutManager(
                        homeActivity, LinearLayoutManager.HORIZONTAL, false
                    )
                    this.eventsRecycler.adapter = RamoEventsAdapter(
                        data = recyclerData, showEventType = true
                    )
                }

                /* calling `Ramo` dialog card clicked */
                this.parentView.setOnClickListener {
                    if (SingletonHelper.isInstanceActive) {
                        return@setOnClickListener
                    }
                    SingletonHelper.isInstanceActive = true

                    homeActivity.intent.putExtra(IntentKeys.RAMO_NRC, ramo.NRC)
                    homeActivity.intent.putExtra(IntentKeys.RAMO_IS_INSCRIBED, true)

                    RamoDialogFragment(
                        onDismissAction = {
                            SingletonHelper.isInstanceActive = false
                            this@RamoEventsExpandedAdapter.notifyDataSetChanged() //! not optimal
                        }
                    ).show(homeActivity.supportFragmentManager, RamoDialogFragment::class.simpleName)
                }
            }
        }
    }
}