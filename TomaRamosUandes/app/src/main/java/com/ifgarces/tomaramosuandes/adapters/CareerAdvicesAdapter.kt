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
import com.ifgarces.tomaramosuandes.models.CareerAdvice
import com.ifgarces.tomaramosuandes.utils.toggleCollapseViewButton


/**
 * RecyclerView adapter for career advices.
 */
class CareerAdvicesAdapter(
    private val data :List<CareerAdvice>, private val activity :HomeActivity
) : RecyclerView.Adapter<CareerAdvicesAdapter.AdviceVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) :AdviceVH {
        return AdviceVH(
            LayoutInflater.from(parent.context).inflate(R.layout.career_advice_item, parent, false)
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

        fun bind(item :CareerAdvice, position :Int) {
            // Displaying advice data
            if (item.image != null) {
                this.imageView.setImageDrawable(item.image)
            } else {
                this.imageView.setImageDrawable(ContextCompat.getDrawable(
                    this@CareerAdvicesAdapter.activity, R.drawable.idea_icon
                ))
            }

            this.headButton.text = item.title
            this.descriptionTxt.text = item.description

            // The body starts collapsed when created
            if (this.isCollapsed) {
                this.bodyContainer.visibility = View.GONE
                this.headButton.icon = ContextCompat.getDrawable(
                    this@CareerAdvicesAdapter.activity, R.drawable.arrow_tip_right
                )
            } else {
                this.bodyContainer.visibility = View.VISIBLE
                this.headButton.icon = ContextCompat.getDrawable(
                    this@CareerAdvicesAdapter.activity, R.drawable.arrow_tip_down
                )
            }

            // Handling collapse/expand click behaviour for the head button
            this.headButton.setOnClickListener {
                this@CareerAdvicesAdapter.activity.toggleCollapseViewButton(
                    isCollapsed = this.isCollapsed,
                    toggleButton = this.headButton,
                    targetContainer = this.bodyContainer
                )
                this.isCollapsed = !this.isCollapsed
            }

            // Handling URI on-click, if existing
            this.parentView.setOnClickListener {
                if (item.uri != null) {
                    this@CareerAdvicesAdapter.activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse(item.uri)
                        )
                    )
                }
            }
        }
    }
}