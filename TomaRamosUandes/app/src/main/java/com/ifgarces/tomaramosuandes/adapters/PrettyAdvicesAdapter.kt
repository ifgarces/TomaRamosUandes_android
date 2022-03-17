package com.ifgarces.tomaramosuandes.adapters

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.models.PrettyAdvice


/**
 * RecyclerView adapter for career advices.
 */
class PrettyAdvicesAdapter(
    private val data :List<PrettyAdvice>, private val activity :HomeActivity
) : RecyclerView.Adapter<PrettyAdvicesAdapter.AdviceVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) :AdviceVH {
        return AdviceVH(
            LayoutInflater.from(parent.context).inflate(R.layout.pretty_advice_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :AdviceVH, position :Int) =
        holder.bind(this.data[position], position)

    inner class AdviceVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView     :View = v
        private val headButton     :MaterialButton = v.findViewById(R.id.prettyAdvice_headBtn)
        private val bodyContainer  :View = v.findViewById(R.id.prettyAdvice_bodyContainer)
        private val imageView      :ImageView = v.findViewById(R.id.prettyAdvice_imageView)
        private val descriptionTxt :TextView = v.findViewById(R.id.prettyAdvice_description)

        private var isCollapsed :Boolean = true // collapsed on creation

        fun bind(item :PrettyAdvice, position :Int) {
            // Displaying advice data
            if (item.image != null) {
                this.imageView.setImageDrawable(item.image)
            } else {
                this.imageView.setImageDrawable(ContextCompat.getDrawable(
                    this@PrettyAdvicesAdapter.activity, R.drawable.idea_icon
                ))
            }

            this.headButton.text = item.title
            this.descriptionTxt.text = item.description

            // The body starts collapsed when created
            if (isCollapsed) {
                this.bodyContainer.visibility = View.GONE
                this.headButton.icon = ContextCompat.getDrawable(
                    this@PrettyAdvicesAdapter.activity, R.drawable.arrow_tip_right
                )
            } else {
                this.bodyContainer.visibility = View.VISIBLE
                this.headButton.icon = ContextCompat.getDrawable(
                    this@PrettyAdvicesAdapter.activity, R.drawable.arrow_tip_down
                )
            }
            //TODO: solve bolerplate code here!!

            // Handling collapse/expand click behaviour for the head button
            this.headButton.setOnClickListener {
                this@PrettyAdvicesAdapter.onCollapseToggleButton(
                    isCollapsed = this.isCollapsed,
                    toggleButton = this.headButton,
                    targetContainer = this.bodyContainer
                )
                this.isCollapsed = !this.isCollapsed
            }

            // Handling URI on-click, if existing
            this.parentView.setOnClickListener {
                if (item.uri != null) {
                    this@PrettyAdvicesAdapter.activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse(item.uri)
                        )
                    )
                }
            }
        }
    }

    /**
     * On-click behaviour for the buttons of the headers for collapsing/expanding `View`s, as well
     * as for updating the button icon.
     * @param isCollapsed Wether the current section is collapsed or not right when the user clicks
     * the collapse/expand toggle button.
     * @param toggleButton Button for collapsing/expanding.
     * @param targetContainer Target view for the elements that are hidden/shown.
     */
    private fun onCollapseToggleButton(
        isCollapsed :Boolean, toggleButton :MaterialButton, targetContainer :View
    ) {
        targetContainer.visibility = if (isCollapsed) View.VISIBLE else View.GONE
        //TODO: add basic appear/dissapear animation
        toggleButton.icon = ContextCompat.getDrawable(
            this.activity,
            if (isCollapsed) R.drawable.arrow_tip_down else R.drawable.arrow_tip_right
        )
    }
}