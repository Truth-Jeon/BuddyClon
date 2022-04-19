package com.mcnex.albatross.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mcnex.albatross.R
import com.mcnex.albatross.bean.ScanBean

class BLEScanAdapter(private val onClick: (Int) -> Unit) : ListAdapter<ScanBean, BLEScanAdapter.BLEViewHolder>(BLEDiffCallback) {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    class BLEViewHolder(itemView: View, val onClick: (Int) -> Unit) :  RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.ble_item_text)
        private val mac: TextView = itemView.findViewById(R.id.ble_item_mac_text)
        private var currentScanBean: ScanBean? = null
        private var currentPosition : Int = 0

        init {
            itemView.setOnClickListener {
                currentPosition.let {
                    onClick(it)
                }
            }
        }

        fun bind(scanBean: ScanBean, position: Int) {
            currentScanBean = scanBean
            currentPosition = position

            name.text = scanBean.name
            mac.text = scanBean.address
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BLEViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ble, parent, false)
        return BLEViewHolder(view, onClick)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: BLEViewHolder, position: Int) {
        val scanBean = getItem(position)
        holder.bind(scanBean, position)
    }

    override fun submitList(list: List<ScanBean>?) {
        super.submitList(list?.let { ArrayList(it) })
    }
}

object BLEDiffCallback : DiffUtil.ItemCallback<ScanBean>() {
    override fun areItemsTheSame(oldItem: ScanBean, newItem: ScanBean): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ScanBean, newItem: ScanBean): Boolean {
        return oldItem.address == newItem.address
    }


}
