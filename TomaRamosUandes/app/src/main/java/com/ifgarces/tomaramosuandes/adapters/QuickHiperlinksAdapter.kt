package com.ifgarces.tomaramosuandes.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.models.QuickHyperlink
import com.ifgarces.tomaramosuandes.utils.DataMaster


/**
 * Adapter for multiple big web links in `DashboardFragment`.
 * @property data The collection of "web links" with image, URI and display name.
 */
class QuickHiperlinksAdapter(
    private val data :List<QuickHyperlink>, private val activity :HomeActivity
) : RecyclerView.Adapter<QuickHiperlinksAdapter.LinkVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) :LinkVH {
        return LinkVH(
            LayoutInflater.from(parent.context).inflate(
                if (DataMaster.userStats.nightModeOn) R.layout.night_quick_hyperlink_item
                else R.layout.quick_hyperlink_item,
                parent, false
            )
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :LinkVH, position :Int) =
        holder.bind(this.data[position], position)

    inner class LinkVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView :View = v
        private val linkImageView :ImageView = v.findViewById(R.id.quickLinkItem_image)
        private val linkTextView :TextView = v.findViewById(R.id.quickLinkItem_text)

        fun bind(item :QuickHyperlink, position :Int) {
            this.linkImageView.setImageDrawable(item.image)
            this.linkTextView.text = item.name
            this.parentView.setOnClickListener {
                this@QuickHiperlinksAdapter.activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(item.uri)
                    )
                )
            }
        }
    }
}