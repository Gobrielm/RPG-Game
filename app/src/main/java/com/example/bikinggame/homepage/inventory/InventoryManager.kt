package com.example.bikinggame.homepage.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bikinggame.R
import com.example.bikinggame.homepage.inventory.InventoryFragment.InventoryMode

data class Item(val imageResId: Int, val text: String)

class InventoryManager(private val items: List<Item>, private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<InventoryManager.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageButton: ImageButton = view.findViewById(R.id.imageButton)
        val text: TextView = view.findViewById(R.id.itemText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.imageButton.setImageResource(item.imageResId)
        holder.imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
        holder.text.text = item.text
        holder.imageButton.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = items.size
}
