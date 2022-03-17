package com.ifgarces.tomaramosuandes.adapters

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.models.PrettyHyperlink


/**
 * Adapter for multiple big web links in `DashboardFragment`.
 * @property data The collection of "web links" with image, URI and display name.
 */
class PrettyLinksAdapter(
    private val data :List<PrettyHyperlink>, private val activity :HomeActivity
) : RecyclerView.Adapter<PrettyLinksAdapter.LinkVH>() {

    override fun onCreateViewHolder(parent :ViewGroup, viewType :Int) :LinkVH {
        return LinkVH(
            LayoutInflater.from(parent.context).inflate(R.layout.pretty_link_item, parent, false)
        )
    }

    override fun getItemCount() = this.data.count()

    override fun onBindViewHolder(holder :LinkVH, position :Int) =
        holder.bind(this.data[position], position)

    inner class LinkVH(v :View) : RecyclerView.ViewHolder(v) {
        private val parentView :View = v
        private val linkImageView :ImageView = v.findViewById(R.id.linkItem_image)
        private val linkTextView :TextView = v.findViewById(R.id.linkItem_text)

        fun bind(item :PrettyHyperlink, position :Int) {
            this.linkImageView.setImageDrawable(item.image)
            this.linkTextView.text = item.name
            this.parentView.setOnClickListener {
                this@PrettyLinksAdapter.activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(item.uri)
                    )
                )
            }
        }
    }
}