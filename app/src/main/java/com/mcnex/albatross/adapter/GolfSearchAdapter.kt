package com.mcnex.albatross.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mcnex.albatross.R
import com.mcnex.albatross.app.App
import com.mcnex.albatross.model.Golf

class GolfSearchAdapter(private val onClick: (Int) -> Unit) : ListAdapter<Golf, GolfSearchAdapter.GolfViewHolder>(GolfDiffCallback) {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    class GolfViewHolder(itemView: View, val onClick: (Int) -> Unit) :  RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val subtitle: TextView = itemView.findViewById(R.id.subtitle)
        private var currentGolf: Golf? = null
        private var currentPosition : Int = 0

        init {
            itemView.setOnClickListener {
                currentPosition.let {
                    onClick(it)
                }
            }
        }

        fun bind(golf: Golf, position: Int) {
            currentGolf = golf
            currentPosition = position


            when(App.APP_LANGUAGE){
                "ko", "ja" -> {
                    title.text = golf.golf_name.golf_native
                    subtitle.text =  String.format("[%s] %s", golf.nation.nation_native, golf.region.region_native)
                }
                else -> {
                    title.text = golf.golf_name.golf_english
                    subtitle.text =  String.format("[%s] %s", golf.nation.nation_english, golf.region.region_english)
                }
            }
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GolfViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_golf_search, parent, false)
        return GolfViewHolder(view, onClick)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: GolfViewHolder, position: Int) {
        val mGolfInfo = getItem(position)
        holder.bind(mGolfInfo, position)
    }

    override fun submitList(list: List<Golf>?) {
        super.submitList(list?.let { ArrayList(it) })
    }
}

object GolfDiffCallback : DiffUtil.ItemCallback<Golf>() {
    override fun areItemsTheSame(oldItem: Golf, newItem: Golf): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Golf, newItem: Golf): Boolean {
        return oldItem.golf_code == newItem.golf_code
    }

}
